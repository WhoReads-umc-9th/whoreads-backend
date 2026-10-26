package whoreads.backend.domain.readingsession;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import whoreads.backend.config.TestFirebaseConfig;
import whoreads.backend.domain.member.entity.Member;
import whoreads.backend.domain.member.enums.AgeGroup;
import whoreads.backend.domain.member.enums.Gender;
import whoreads.backend.domain.member.repository.MemberRepository;
import whoreads.backend.domain.readingsession.service.ReadingSessionScheduler;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

/**
 * 타이머 시작/일시정지/완료 API를 실제 HTTP 흐름대로 호출해보는 시나리오 테스트 (H2).
 * 시간 경과는 interval.start_time / last_heartbeat_at 을 과거로 당겨서 흉내낸다.
 */
@SpringBootTest
@Import(TestFirebaseConfig.class)
class ReadingSessionTimerScenarioTest {

    @Autowired WebApplicationContext ctx;
    MockMvc mockMvc;
    @Autowired MemberRepository memberRepository;
    @Autowired JdbcTemplate jdbc;
    @Autowired ReadingSessionScheduler scheduler;
    ObjectMapper objectMapper = new ObjectMapper();

    Long memberId;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(ctx).build();
        String u = UUID.randomUUID().toString().substring(0, 8);
        memberId = memberRepository.save(Member.builder()
                .nickname("t" + u).gender(Gender.FEMALE).ageGroup(AgeGroup.TWENTIES)
                .email(u + "@test.com").loginId("id" + u).password("pw").build()).getId();
    }

    @Test
    void A_heartbeat_꾸준히_보내고_25분_후_완료() throws Exception {
        long sid = start();
        shiftStart(sid, 25);
        setHeartbeatMinutesAgo(sid, 0);           // heartbeat 정상 전송 중
        scheduler.expireStaleSessions();
        call("A complete", post("/api/reading-sessions/{id}/complete", sid));
        dump("A", sid);
    }

    @Test
    void B_heartbeat_없이_25분_후_완료() throws Exception {
        long sid = start();
        shiftStart(sid, 25);
        setHeartbeatMinutesAgo(sid, 25);          // 시작 이후 heartbeat 한 번도 없음
        scheduler.expireStaleSessions();          // 5분 기준 스케줄러 1회 실행
        dump("B (스케줄러 후)", sid);
        call("B complete", post("/api/reading-sessions/{id}/complete", sid));
        dump("B", sid);
    }

    @Test
    void C_10분_읽고_일시정지_후_바로_저장() throws Exception {
        long sid = start();
        shiftStart(sid, 10);
        setHeartbeatMinutesAgo(sid, 0);
        call("C pause", post("/api/reading-sessions/{id}/pause", sid));
        call("C complete", post("/api/reading-sessions/{id}/complete", sid));
        dump("C", sid);
    }

    @Test
    void D_완료_두번_호출() throws Exception {
        long sid = start();
        call("D complete#1", post("/api/reading-sessions/{id}/complete", sid));
        call("D complete#2", post("/api/reading-sessions/{id}/complete", sid));
    }

    @Test
    void E_recover_경로_슬래시() throws Exception {
        long sid = start();
        call("E recover (no slash)", get("/api/reading-sessions/{id}/recover", sid));
        call("E recover/ (slash)", get("/api/reading-sessions/{id}/recover/", sid));
    }

    @Test
    void F_월별기록_반영_확인() throws Exception {
        long sid = start();
        shiftStart(sid, 15);
        setHeartbeatMinutesAgo(sid, 0);
        call("F complete", post("/api/reading-sessions/{id}/complete", sid));
        LocalDate now = LocalDate.now();
        call("F monthly", get("/api/me/reading-sessions/records/monthly")
                .param("year", String.valueOf(now.getYear()))
                .param("month", String.valueOf(now.getMonthValue())));
    }

    @Test
    void G_heartbeat_API로_7분_타이머_완료() throws Exception {
        long sid = start();
        shiftStart(sid, 7);
        setHeartbeatMinutesAgo(sid, 6);           // 6분 전까지 끊겼다가
        call("G heartbeat", patch("/api/reading-sessions/{id}/heartbeat", sid)); // 실제 API로 heartbeat
        scheduler.expireStaleSessions();
        dump("G (스케줄러 후)", sid);
        call("G complete", post("/api/reading-sessions/{id}/complete", sid));
        dump("G", sid);
    }

    @Test
    void H_9분59초_한구간_완료() throws Exception {
        long sid = start();
        shiftOpenIntervalSeconds(sid, 599);
        setHeartbeatMinutesAgo(sid, 0);
        call("H complete", post("/api/reading-sessions/{id}/complete", sid), 200);
        dump("H (기대 10분)", sid);
        assertThat(totalMinutes(sid)).isEqualTo(10L);
    }

    @Test
    void I_3분40초씩_세구간_완료() throws Exception {
        long sid = start();
        setHeartbeatMinutesAgo(sid, 0);
        shiftOpenIntervalSeconds(sid, 220);
        call("I pause#1", post("/api/reading-sessions/{id}/pause", sid));
        call("I resume#1", post("/api/reading-sessions/{id}/resume", sid));
        shiftOpenIntervalSeconds(sid, 220);
        call("I pause#2", post("/api/reading-sessions/{id}/pause", sid));
        call("I resume#2", post("/api/reading-sessions/{id}/resume", sid));
        shiftOpenIntervalSeconds(sid, 220);
        call("I complete", post("/api/reading-sessions/{id}/complete", sid), 200);
        dump("I (기대 11분)", sid);
        assertThat(totalMinutes(sid)).isEqualTo(11L);
    }

    // ---- helpers ----

    private void shiftOpenIntervalSeconds(long sid, int seconds) {
        jdbc.update("UPDATE reading_interval SET start_time = DATEADD('SECOND', ?, start_time) WHERE session_id = ? AND end_time IS NULL", -seconds, sid);
    }

    private long start() throws Exception {
        JsonNode body = call("start", post("/api/reading-sessions/start"));
        return body.path("result").path("session_id").asLong();
    }

    private void shiftStart(long sid, int minutes) {
        jdbc.update("UPDATE reading_interval SET start_time = DATEADD('MINUTE', ?, start_time) WHERE session_id = ?", -minutes, sid);
    }

    private void setHeartbeatMinutesAgo(long sid, int minutes) {
        jdbc.update("UPDATE reading_session SET last_heartbeat_at = DATEADD('MINUTE', ?, CURRENT_TIMESTAMP) WHERE id = ?", -minutes, sid);
    }

    private JsonNode call(String label, MockHttpServletRequestBuilder req) throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(memberId, null, List.of()));
        MvcResult r = mockMvc.perform(req).andReturn();
        String body = r.getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
        System.out.printf("[SCENARIO] %-22s -> %d %s%n", label, r.getResponse().getStatus(), body);
        return body.isBlank() ? objectMapper.createObjectNode() : objectMapper.readTree(body);
    }

    // 기대 상태를 호출마다 지정 (공용 call은 D처럼 실패가 예상되는 호출에도 쓰이므로 상태를 고정하지 않음)
    private JsonNode call(String label, MockHttpServletRequestBuilder req, int expectedStatus) throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(memberId, null, List.of()));
        MvcResult r = mockMvc.perform(req).andReturn();
        String body = r.getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
        System.out.printf("[SCENARIO] %-22s -> %d %s%n", label, r.getResponse().getStatus(), body);
        assertThat(r.getResponse().getStatus()).as(label).isEqualTo(expectedStatus);
        return body.isBlank() ? objectMapper.createObjectNode() : objectMapper.readTree(body);
    }

    private Long totalMinutes(long sid) {
        return jdbc.queryForObject("SELECT total_minutes FROM reading_session WHERE id = ?", Long.class, sid);
    }

    private void dump(String label, long sid) {
        System.out.printf("[SCENARIO] %-22s -> session=%s%n", label + " DB",
                jdbc.queryForMap("SELECT status, total_minutes, finished_at FROM reading_session WHERE id = ?", sid));
        jdbc.queryForList("SELECT start_time, end_time, duration_minutes FROM reading_interval WHERE session_id = ? ORDER BY id", sid)
                .forEach(i -> System.out.printf("[SCENARIO] %-22s    interval=%s%n", "", i));
    }
}
