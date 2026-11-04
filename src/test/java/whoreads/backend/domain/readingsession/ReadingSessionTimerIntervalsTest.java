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
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * 세션 시작 body로 받은 heartbeat 주기 / 중단 전환 주기 / 완료 전환 주기가 세션별로 저장되고
 * 스케줄러·재개 판정에 적용되는지 검증한다 (H2).
 */
@SpringBootTest
@Import(TestFirebaseConfig.class)
class ReadingSessionTimerIntervalsTest {

    @Autowired WebApplicationContext ctx;
    @Autowired MemberRepository memberRepository;
    @Autowired JdbcTemplate jdbc;
    @Autowired ReadingSessionScheduler scheduler;
    ObjectMapper objectMapper = new ObjectMapper();
    MockMvc mockMvc;

    Long owner;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(ctx).build();
        owner = newMember();
    }

    @Test
    @DisplayName("시작 body로 보낸 세 주기가 세션에 저장된다")
    void start_savesIntervalsFromBody() throws Exception {
        long sid = start("{\"heartbeat_interval_minutes\":2,\"suspend_after_minutes\":10,\"complete_after_minutes\":30}");

        Map<String, Object> row = intervals(sid);
        assertThat(((Number) row.get("HEARTBEAT_INTERVAL_MINUTES")).longValue()).isEqualTo(2L);
        assertThat(((Number) row.get("SUSPEND_AFTER_MINUTES")).longValue()).isEqualTo(10L);
        assertThat(((Number) row.get("COMPLETE_AFTER_MINUTES")).longValue()).isEqualTo(30L);
    }

    @Test
    @DisplayName("body를 생략하거나 일부만 보내면 나머지는 기본값(1/5/120)이 적용된다")
    void start_withoutBodyOrPartialBody_usesDefaults() throws Exception {
        Map<String, Object> noBody = intervals(start(null));
        assertThat(((Number) noBody.get("HEARTBEAT_INTERVAL_MINUTES")).longValue()).isEqualTo(1L);
        assertThat(((Number) noBody.get("SUSPEND_AFTER_MINUTES")).longValue()).isEqualTo(5L);
        assertThat(((Number) noBody.get("COMPLETE_AFTER_MINUTES")).longValue()).isEqualTo(120L);

        owner = newMember();
        Map<String, Object> partial = intervals(start("{\"complete_after_minutes\":45}"));
        assertThat(((Number) partial.get("SUSPEND_AFTER_MINUTES")).longValue()).isEqualTo(5L);
        assertThat(((Number) partial.get("COMPLETE_AFTER_MINUTES")).longValue()).isEqualTo(45L);
    }

    @Test
    @DisplayName("범위를 벗어났거나 heartbeat ≤ 중단 ≤ 완료 순서를 어기면 400")
    void start_invalidIntervals_badRequest() throws Exception {
        assertThat(startStatus("{\"heartbeat_interval_minutes\":0}")).isEqualTo(400);
        assertThat(startStatus("{\"suspend_after_minutes\":1441}")).isEqualTo(400);
        assertThat(startStatus("{\"complete_after_minutes\":-1}")).isEqualTo(400);
        assertThat(startStatus("{\"heartbeat_interval_minutes\":10,\"suspend_after_minutes\":5}")).isEqualTo(400);
        // 기본 중단 전환 주기(5분)보다 긴 heartbeat 주기만 보내도 같은 이유로 거절된다
        assertThat(startStatus("{\"heartbeat_interval_minutes\":10}")).isEqualTo(400);
        // 완료 전환 주기가 중단 전환 주기보다 짧으면 중단되기 전에 이어하기만 만료되어 상태가 어긋난다
        assertThat(startStatus("{\"suspend_after_minutes\":30,\"complete_after_minutes\":10}")).isEqualTo(400);
        // 기본 중단 전환 주기(5분)보다 짧은 완료 전환 주기만 보내도 같은 이유로 거절된다
        assertThat(startStatus("{\"complete_after_minutes\":3}")).isEqualTo(400);
    }

    @Test
    @DisplayName("상한 1440분과 같은 값(heartbeat = 중단 = 완료)은 허용되고 세션에 저장된다")
    void start_boundaryValues_accepted() throws Exception {
        Map<String, Object> max = intervals(start("{\"heartbeat_interval_minutes\":1440,\"suspend_after_minutes\":1440,\"complete_after_minutes\":1440}"));
        assertThat(((Number) max.get("HEARTBEAT_INTERVAL_MINUTES")).longValue()).isEqualTo(1440L);
        assertThat(((Number) max.get("SUSPEND_AFTER_MINUTES")).longValue()).isEqualTo(1440L);
        assertThat(((Number) max.get("COMPLETE_AFTER_MINUTES")).longValue()).isEqualTo(1440L);

        owner = newMember();
        Map<String, Object> min = intervals(start("{\"heartbeat_interval_minutes\":1,\"suspend_after_minutes\":1,\"complete_after_minutes\":1}"));
        assertThat(((Number) min.get("HEARTBEAT_INTERVAL_MINUTES")).longValue()).isEqualTo(1L);
        assertThat(((Number) min.get("SUSPEND_AFTER_MINUTES")).longValue()).isEqualTo(1L);
        assertThat(((Number) min.get("COMPLETE_AFTER_MINUTES")).longValue()).isEqualTo(1L);
    }

    @Test
    @DisplayName("스케줄러는 세션별 suspend_after_minutes 기준으로 중단 처리한다")
    void scheduler_suspendsBySessionSuspendAfter() throws Exception {
        long longWindow = start("{\"suspend_after_minutes\":30}");
        owner = newMember();
        long shortWindow = start("{\"suspend_after_minutes\":5}");
        // 두 세션 모두 마지막 heartbeat가 10분 전
        setHeartbeatMinutesAgo(longWindow, 10);
        setHeartbeatMinutesAgo(shortWindow, 10);

        scheduler.expireStaleSessions();

        assertThat(row(longWindow).get("STATUS")).isEqualTo("IN_PROGRESS");
        assertThat(row(shortWindow).get("STATUS")).isEqualTo("SUSPENDED");
    }

    @Test
    @DisplayName("스케줄러는 세션별 complete_after_minutes 기준으로 일시정지 세션을 완료한다")
    void scheduler_completesPausedBySessionCompleteAfter() throws Exception {
        long shortWindow = pausedSession("{\"complete_after_minutes\":30}", 50, 40);   // 40분 전 일시정지
        owner = newMember();
        long defaultWindow = pausedSession(null, 50, 40);

        scheduler.completeAbandonedPausedSessions();

        assertThat(row(shortWindow).get("STATUS")).isEqualTo("COMPLETED");
        assertThat(row(defaultWindow).get("STATUS")).isEqualTo("PAUSED");
    }

    @Test
    @DisplayName("중단된 세션 재개는 세션의 complete_after_minutes 이내일 때만 가능하다")
    void recover_respectsSessionCompleteAfter() throws Exception {
        long expired = start("{\"complete_after_minutes\":30}");
        setHeartbeatMinutesAgo(expired, 40);
        assertThat(status(get("/api/reading-sessions/{id}/recover/", expired))).isEqualTo(400);

        owner = newMember();
        long recoverable = start("{\"complete_after_minutes\":60}");
        setHeartbeatMinutesAgo(recoverable, 40);
        assertThat(status(get("/api/reading-sessions/{id}/recover/", recoverable))).isEqualTo(200);
    }

    // ---- helpers ----

    // startedMinutesAgo분 전에 시작해 pausedMinutesAgo분 전에 일시정지한 세션
    private long pausedSession(String body, int startedMinutesAgo, int pausedMinutesAgo) throws Exception {
        long sid = start(body);
        jdbc.update("UPDATE reading_interval SET start_time = DATEADD('MINUTE', ?, start_time) WHERE session_id = ?", -startedMinutesAgo, sid);
        assertThat(status(post("/api/reading-sessions/{id}/pause", sid))).isEqualTo(200);
        jdbc.update("UPDATE reading_interval SET end_time = DATEADD('MINUTE', ?, end_time), duration_minutes = duration_minutes - ? WHERE session_id = ?",
                -pausedMinutesAgo, pausedMinutesAgo, sid);
        return sid;
    }

    private void setHeartbeatMinutesAgo(long sid, int minutes) {
        jdbc.update("UPDATE reading_session SET last_heartbeat_at = DATEADD('MINUTE', ?, CURRENT_TIMESTAMP) WHERE id = ?", -minutes, sid);
    }

    private Long newMember() {
        String u = UUID.randomUUID().toString().substring(0, 8);
        return memberRepository.save(Member.builder()
                .nickname("t" + u).gender(Gender.FEMALE).ageGroup(AgeGroup.TWENTIES)
                .email(u + "@test.com").loginId("id" + u).password("pw").build()).getId();
    }

    private MockHttpServletRequestBuilder startRequest(String body) {
        MockHttpServletRequestBuilder req = post("/api/reading-sessions/start");
        return body == null ? req : req.contentType(MediaType.APPLICATION_JSON).content(body);
    }

    private long start(String body) throws Exception {
        authenticate();
        String response = mockMvc.perform(startRequest(body)).andReturn()
                .getResponse().getContentAsString(StandardCharsets.UTF_8);
        return objectMapper.readTree(response).path("result").path("session_id").asLong();
    }

    private int startStatus(String body) throws Exception {
        return status(startRequest(body));
    }

    private int status(MockHttpServletRequestBuilder req) throws Exception {
        authenticate();
        return mockMvc.perform(req).andReturn().getResponse().getStatus();
    }

    private void authenticate() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(owner, null, List.of()));
    }

    private Map<String, Object> row(long sid) {
        return jdbc.queryForMap("SELECT status FROM reading_session WHERE id = ?", sid);
    }

    private Map<String, Object> intervals(long sid) {
        return jdbc.queryForMap(
                "SELECT heartbeat_interval_minutes, suspend_after_minutes, complete_after_minutes FROM reading_session WHERE id = ?", sid);
    }
}
