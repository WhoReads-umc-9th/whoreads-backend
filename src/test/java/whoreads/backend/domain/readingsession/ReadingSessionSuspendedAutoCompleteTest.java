package whoreads.backend.domain.readingsession;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
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
 * 이슈 #243: 새 세션 시작 시 남아 있던 SUSPENDED 세션 자동 완료 회귀 테스트 (H2).
 * 중단(SUSPENDED) 상태는 heartbeat를 150분 전으로 두고 스케줄러를 돌려 만든다 (stale 기준 5분/2시간 모두 해당).
 */
@SpringBootTest
@Import(TestFirebaseConfig.class)
class ReadingSessionSuspendedAutoCompleteTest {

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
    @DisplayName("새 세션을 시작하면 SUSPENDED 세션이 마지막 heartbeat 기준으로 완료되고 종료 시각이 유지된다")
    void start_completesSuspendedSession_keepingLastHeartbeatAsFinishedAt() throws Exception {
        long suspended = suspendedSession(180, 150);   // 30분 읽고 150분 전부터 끊김
        Map<String, Object> before = row(suspended);

        long next = start();

        Map<String, Object> after = row(suspended);
        assertThat(after.get("STATUS")).isEqualTo("COMPLETED");
        assertThat(((Number) after.get("TOTAL_MINUTES")).longValue()).isEqualTo(30L);
        assertThat(after.get("FINISHED_AT")).isEqualTo(before.get("LAST_HEARTBEAT_AT"));
        assertThat(row(next).get("STATUS")).isEqualTo("IN_PROGRESS");
    }

    @Test
    @DisplayName("새 세션을 완료한 뒤 미완료 세션 조회에 예전 SUSPENDED 세션이 다시 나오지 않는다")
    void incomplete_doesNotReturnOldSuspendedSession() throws Exception {
        suspendedSession(180, 150);
        long next = start();
        assertThat(status(post("/api/reading-sessions/{id}/complete", next))).isEqualTo(200);

        assertThat(status(get("/api/reading-sessions/incomplete"))).isEqualTo(404);
    }

    @Test
    @DisplayName("다른 회원의 SUSPENDED 세션은 건드리지 않는다")
    void start_doesNotTouchOtherMembersSuspendedSession() throws Exception {
        long othersSuspended = suspendedSession(180, 150);
        owner = newMember();

        start();

        assertThat(row(othersSuspended).get("STATUS")).isEqualTo("SUSPENDED");
    }

    // ---- helpers ----

    private long suspendedSession(int startedMinutesAgo, int heartbeatMinutesAgo) throws Exception {
        long sid = start();
        jdbc.update("UPDATE reading_interval SET start_time = DATEADD('MINUTE', ?, start_time) WHERE session_id = ?", -startedMinutesAgo, sid);
        jdbc.update("UPDATE reading_session SET last_heartbeat_at = DATEADD('MINUTE', ?, CURRENT_TIMESTAMP) WHERE id = ?", -heartbeatMinutesAgo, sid);
        scheduler.expireStaleSessions();
        assertThat(row(sid).get("STATUS")).isEqualTo("SUSPENDED");
        return sid;
    }

    private Long newMember() {
        String u = UUID.randomUUID().toString().substring(0, 8);
        return memberRepository.save(Member.builder()
                .nickname("t" + u).gender(Gender.FEMALE).ageGroup(AgeGroup.TWENTIES)
                .email(u + "@test.com").loginId("id" + u).password("pw").build()).getId();
    }

    private long start() throws Exception {
        authenticate();
        String body = mockMvc.perform(post("/api/reading-sessions/start")).andReturn()
                .getResponse().getContentAsString(StandardCharsets.UTF_8);
        return objectMapper.readTree(body).path("result").path("session_id").asLong();
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
        return jdbc.queryForMap("SELECT status, total_minutes, finished_at, last_heartbeat_at FROM reading_session WHERE id = ?", sid);
    }
}
