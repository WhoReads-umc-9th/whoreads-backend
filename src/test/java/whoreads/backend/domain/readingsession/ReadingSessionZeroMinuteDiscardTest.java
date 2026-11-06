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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * 반올림해서 0분이 되는 세션(30초 미만)은 어떤 완료 경로로든 저장되지 않는지 검증 (H2).
 */
@SpringBootTest
@Import(TestFirebaseConfig.class)
class ReadingSessionZeroMinuteDiscardTest {

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
    @DisplayName("25초 읽고 완료하면 세션과 구간이 저장되지 않는다")
    void complete_under30Seconds_discardsSession() throws Exception {
        long session = startedSecondsAgo(25);

        assertThat(status(post("/api/reading-sessions/{id}/complete", session))).isEqualTo(200);

        assertThat(sessionCount(session)).isZero();
        assertThat(intervalCount(session)).isZero();
    }

    @Test
    @DisplayName("30초 읽고 완료하면 1분으로 반올림되어 저장된다")
    void complete_30Seconds_keepsSession() throws Exception {
        long session = startedSecondsAgo(30);

        assertThat(status(post("/api/reading-sessions/{id}/complete", session))).isEqualTo(200);

        assertThat(jdbc.queryForObject("SELECT total_minutes FROM reading_session WHERE id = ?", Long.class, session))
                .isEqualTo(1L);
    }

    @Test
    @DisplayName("0분으로 일시정지된 세션은 새 세션을 시작할 때 삭제되고 새 세션은 진행된다")
    void start_discardsZeroMinuteLeftover() throws Exception {
        long paused = start();
        assertThat(status(post("/api/reading-sessions/{id}/pause", paused))).isEqualTo(200);

        long next = start();

        assertThat(sessionCount(paused)).isZero();
        assertThat(sessionCount(next)).isEqualTo(1);
    }

    @Test
    @DisplayName("0분으로 일시정지된 세션을 공백 처리로 완료하면 삭제된다")
    void completeIdleTime_discardsZeroMinutePausedSession() throws Exception {
        long paused = start();
        assertThat(status(post("/api/reading-sessions/{id}/pause", paused))).isEqualTo(200);

        assertThat(status(patch("/api/reading-sessions/{id}/complete-idle-time", paused))).isEqualTo(200);

        assertThat(sessionCount(paused)).isZero();
    }

    @Test
    @DisplayName("방치된 0분 일시정지 세션은 스케줄러가 완료할 때 삭제된다")
    void scheduler_discardsAbandonedZeroMinutePausedSession() throws Exception {
        long paused = start();
        assertThat(status(post("/api/reading-sessions/{id}/pause", paused))).isEqualTo(200);
        jdbc.update("UPDATE reading_interval SET start_time = DATEADD('MINUTE', -150, start_time), "
                + "end_time = DATEADD('MINUTE', -150, end_time) WHERE session_id = ?", paused);

        scheduler.completeAbandonedPausedSessions();

        assertThat(sessionCount(paused)).isZero();
    }

    private long startedSecondsAgo(int seconds) throws Exception {
        long session = start();
        jdbc.update("UPDATE reading_interval SET start_time = DATEADD('SECOND', ?, start_time) WHERE session_id = ?",
                -seconds, session);
        return session;
    }

    private long start() throws Exception {
        String body = mockMvc.perform(authed(post("/api/reading-sessions/start")))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        return objectMapper.readTree(body).at("/result/session_id").asLong();
    }

    private int status(MockHttpServletRequestBuilder request) throws Exception {
        return mockMvc.perform(authed(request)).andReturn().getResponse().getStatus();
    }

    private MockHttpServletRequestBuilder authed(MockHttpServletRequestBuilder request) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(owner, null, List.of()));
        return request;
    }

    private int sessionCount(long sessionId) {
        return jdbc.queryForObject("SELECT COUNT(*) FROM reading_session WHERE id = ?", Integer.class, sessionId);
    }

    private int intervalCount(long sessionId) {
        return jdbc.queryForObject("SELECT COUNT(*) FROM reading_interval WHERE session_id = ?", Integer.class, sessionId);
    }

    private Long newMember() {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        return memberRepository.save(Member.builder()
                .email(suffix + "@test.com")
                .loginId("user" + suffix)
                .nickname("nick" + suffix)
                .password("password")
                .gender(Gender.MALE)
                .ageGroup(AgeGroup.TWENTIES)
                .build()).getId();
    }
}
