package whoreads.backend.domain.readingsession;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import whoreads.backend.config.TestFirebaseConfig;
import whoreads.backend.domain.member.entity.Member;
import whoreads.backend.domain.member.enums.AgeGroup;
import whoreads.backend.domain.member.enums.Gender;
import whoreads.backend.domain.member.repository.MemberRepository;
import whoreads.backend.domain.readingsession.service.ReadingSessionScheduler;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * 이슈 #242: complete-idle-time 권한/열린 구간 처리와 종료된 세션 heartbeat 거절 회귀 테스트 (H2).
 * 시간 경과는 interval.start_time / last_heartbeat_at 을 과거로 당겨서 흉내낸다.
 * 중단(SUSPENDED) 상태는 heartbeat를 150분 전으로 두고 스케줄러를 돌려 만든다 (stale 기준 5분/2시간 모두 해당).
 */
@SpringBootTest
@Import(TestFirebaseConfig.class)
class ReadingSessionIdleTimeHeartbeatTest {

    @Autowired WebApplicationContext ctx;
    @Autowired MemberRepository memberRepository;
    @Autowired JdbcTemplate jdbc;
    @Autowired ReadingSessionScheduler scheduler;
    ObjectMapper objectMapper = new ObjectMapper();
    MockMvc mockMvc;

    Long owner;
    Long other;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(ctx).build();
        owner = newMember();
        other = newMember();
    }

    @Test
    @DisplayName("다른 회원은 complete-idle-time으로 남의 세션을 완료할 수 없다")
    void idleTime_otherMember_forbidden() throws Exception {
        long sid = start(owner);

        assertThat(status(other, patch("/api/reading-sessions/{id}/complete-idle-time", sid))).isEqualTo(403);
        assertThat(sessionStatus(sid)).isEqualTo("IN_PROGRESS");
    }

    @Test
    @DisplayName("진행 중 세션에 complete-idle-time을 호출하면 시작~현재 전체가 저장되고 열린 구간이 남지 않는다")
    void idleTime_inProgress_countsWholeOpenInterval() throws Exception {
        long sid = start(owner);
        shiftStartMinutes(sid, 20);
        heartbeatMinutesAgo(sid, 3);

        assertThat(status(owner, patch("/api/reading-sessions/{id}/complete-idle-time", sid))).isEqualTo(200);
        assertThat(sessionStatus(sid)).isEqualTo("COMPLETED");
        assertThat(totalMinutes(sid)).isEqualTo(20L);
        assertThat(openIntervalCount(sid)).isZero();
    }

    @Test
    @DisplayName("2시간 안에 중단된 세션에 complete-idle-time을 호출하면 마지막 heartbeat 이후 공백까지 저장된다")
    void idleTime_suspended_addsIdleInterval() throws Exception {
        long sid = start(owner);
        shiftStartMinutes(sid, 60);
        heartbeatMinutesAgo(sid, 10);
        scheduler.expireStaleSessions();
        assertThat(sessionStatus(sid)).isEqualTo("SUSPENDED");

        assertThat(status(owner, patch("/api/reading-sessions/{id}/complete-idle-time", sid))).isEqualTo(200);
        assertThat(sessionStatus(sid)).isEqualTo("COMPLETED");
        assertThat(totalMinutes(sid)).isEqualTo(60L);
    }

    @Test
    @DisplayName("2시간이 지난 중단 세션도 공백을 채울 수 있고, 타이머 목표 시간까지만 채운다")
    void idleTime_suspended_cappedByTimerGoal() throws Exception {
        setTimerMinutes(owner, 60);
        long sid = start(owner);
        shiftStartMinutes(sid, 180);
        heartbeatMinutesAgo(sid, 150);              // 30분 읽고 150분 공백
        scheduler.expireStaleSessions();

        assertThat(status(owner, patch("/api/reading-sessions/{id}/complete-idle-time", sid))).isEqualTo(200);
        assertThat(sessionStatus(sid)).isEqualTo("COMPLETED");
        // 30분 + 공백 30분 = 목표 60분. 구간별 분 단위 내림이 남아 있는 동안(#239 머지 전)은 59분이 될 수 있다
        assertThat(totalMinutes(sid)).isBetween(59L, 60L);
    }

    @Test
    @DisplayName("타이머 목표를 설정하지 않았으면 공백 전체를 채운다")
    void idleTime_suspended_noTimerGoal_fillsWholeGap() throws Exception {
        long sid = start(owner);
        shiftStartMinutes(sid, 180);
        heartbeatMinutesAgo(sid, 150);
        scheduler.expireStaleSessions();

        assertThat(status(owner, patch("/api/reading-sessions/{id}/complete-idle-time", sid))).isEqualTo(200);
        assertThat(totalMinutes(sid)).isEqualTo(180L);
    }

    @Test
    @DisplayName("진행 중 세션의 공백도 타이머 목표 시간까지만 채운다")
    void idleTime_inProgress_cappedByTimerGoal() throws Exception {
        setTimerMinutes(owner, 18);
        long sid = start(owner);
        shiftStartMinutes(sid, 20);
        heartbeatMinutesAgo(sid, 3);                 // 17분 확인, 공백 3분

        assertThat(status(owner, patch("/api/reading-sessions/{id}/complete-idle-time", sid))).isEqualTo(200);
        assertThat(totalMinutes(sid)).isEqualTo(18L);
        assertThat(openIntervalCount(sid)).isZero();
    }

    @Test
    @DisplayName("일시정지 세션에 complete-idle-time을 호출하면 일시정지 시간은 더하지 않고 완료한다")
    void idleTime_paused_doesNotCountPausedTime() throws Exception {
        long sid = start(owner);
        shiftStartMinutes(sid, 30);
        assertThat(status(owner, post("/api/reading-sessions/{id}/pause", sid))).isEqualTo(200);
        // 10분 읽고 20분째 일시정지 중인 상태로 맞춤 (일시정지하면 앱이 heartbeat를 멈춤)
        jdbc.update("UPDATE reading_interval SET end_time = DATEADD('MINUTE', -20, end_time), duration_minutes = duration_minutes - 20 WHERE session_id = ?", sid);
        heartbeatMinutesAgo(sid, 20);

        assertThat(status(owner, patch("/api/reading-sessions/{id}/complete-idle-time", sid))).isEqualTo(200);
        assertThat(sessionStatus(sid)).isEqualTo("COMPLETED");
        assertThat(totalMinutes(sid)).isEqualTo(10L);
    }

    @Test
    @DisplayName("재개 직후 첫 heartbeat 전에 중단된 세션도 일시정지 시간은 독서 시간으로 채우지 않는다")
    void idleTime_resumedThenSuspendedBeforeHeartbeat_doesNotCountPausedTime() throws Exception {
        long sid = start(owner);
        shiftStartMinutes(sid, 30);
        assertThat(status(owner, post("/api/reading-sessions/{id}/pause", sid))).isEqualTo(200);
        // 10분 읽고 20분째 일시정지 중인 상태로 맞춤 (일시정지하면 앱이 heartbeat를 멈춤)
        jdbc.update("UPDATE reading_interval SET end_time = DATEADD('MINUTE', -20, end_time), duration_minutes = duration_minutes - 20 WHERE session_id = ?", sid);
        heartbeatMinutesAgo(sid, 20);

        // 재개는 heartbeat를 갱신하지 않으므로 첫 heartbeat 전에 스케줄러가 돌면 방금 재개한 세션이 중단된다
        assertThat(status(owner, post("/api/reading-sessions/{id}/resume", sid))).isEqualTo(200);
        scheduler.expireStaleSessions();
        assertThat(sessionStatus(sid)).isEqualTo("SUSPENDED");

        assertThat(status(owner, patch("/api/reading-sessions/{id}/complete-idle-time", sid))).isEqualTo(200);
        assertThat(sessionStatus(sid)).isEqualTo("COMPLETED");
        assertThat(totalMinutes(sid)).isEqualTo(10L);
    }

    @Test
    @DisplayName("진행 중·일시정지 세션의 heartbeat는 받는다")
    void heartbeat_activeSession_ok() throws Exception {
        long sid = start(owner);
        assertThat(status(owner, patch("/api/reading-sessions/{id}/heartbeat", sid))).isEqualTo(200);

        assertThat(status(owner, post("/api/reading-sessions/{id}/pause", sid))).isEqualTo(200);
        assertThat(status(owner, patch("/api/reading-sessions/{id}/heartbeat", sid))).isEqualTo(200);
    }

    @Test
    @DisplayName("완료된 세션의 heartbeat는 409로 거절한다")
    void heartbeat_completedSession_conflict() throws Exception {
        long sid = start(owner);
        assertThat(status(owner, post("/api/reading-sessions/{id}/complete", sid))).isEqualTo(200);

        assertThat(status(owner, patch("/api/reading-sessions/{id}/heartbeat", sid))).isEqualTo(409);
    }

    @Test
    @DisplayName("중단된 세션의 heartbeat는 409로 거절하고 마지막 heartbeat 시각을 바꾸지 않는다")
    void heartbeat_suspendedSession_conflict() throws Exception {
        long sid = start(owner);
        shiftStartMinutes(sid, 180);
        heartbeatMinutesAgo(sid, 150);
        scheduler.expireStaleSessions();
        Object before = jdbc.queryForObject("SELECT last_heartbeat_at FROM reading_session WHERE id = ?", Object.class, sid);

        assertThat(status(owner, patch("/api/reading-sessions/{id}/heartbeat", sid))).isEqualTo(409);
        assertThat(jdbc.queryForObject("SELECT last_heartbeat_at FROM reading_session WHERE id = ?", Object.class, sid))
                .isEqualTo(before);
    }

    // ---- helpers ----

    private Long newMember() {
        String u = UUID.randomUUID().toString().substring(0, 8);
        return memberRepository.save(Member.builder()
                .nickname("t" + u).gender(Gender.FEMALE).ageGroup(AgeGroup.TWENTIES)
                .email(u + "@test.com").loginId("id" + u).password("pw").build()).getId();
    }

    private long start(Long memberId) throws Exception {
        authenticate(memberId);
        String body = mockMvc.perform(post("/api/reading-sessions/start")).andReturn()
                .getResponse().getContentAsString(StandardCharsets.UTF_8);
        return objectMapper.readTree(body).path("result").path("session_id").asLong();
    }

    private int status(Long memberId, MockHttpServletRequestBuilder req) throws Exception {
        authenticate(memberId);
        return mockMvc.perform(req).andReturn().getResponse().getStatus();
    }

    private void setTimerMinutes(Long memberId, long minutes) throws Exception {
        assertThat(status(memberId, patch("/api/me/reading-sessions/settings/time")
                .contentType(MediaType.APPLICATION_JSON).content("{\"time\":" + minutes + "}"))).isEqualTo(200);
    }

    private void authenticate(Long memberId) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(memberId, null, List.of()));
    }

    private void shiftStartMinutes(long sid, int minutes) {
        jdbc.update("UPDATE reading_interval SET start_time = DATEADD('MINUTE', ?, start_time) WHERE session_id = ?", -minutes, sid);
    }

    private void heartbeatMinutesAgo(long sid, int minutes) {
        jdbc.update("UPDATE reading_session SET last_heartbeat_at = DATEADD('MINUTE', ?, CURRENT_TIMESTAMP) WHERE id = ?", -minutes, sid);
    }

    private String sessionStatus(long sid) {
        return jdbc.queryForObject("SELECT status FROM reading_session WHERE id = ?", String.class, sid);
    }

    private Long totalMinutes(long sid) {
        return jdbc.queryForObject("SELECT total_minutes FROM reading_session WHERE id = ?", Long.class, sid);
    }

    private int openIntervalCount(long sid) {
        return jdbc.queryForObject("SELECT COUNT(*) FROM reading_interval WHERE session_id = ? AND end_time IS NULL", Integer.class, sid);
    }
}
