package whoreads.backend.domain.readingsession;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import whoreads.backend.config.TestFirebaseConfig;
import whoreads.backend.domain.focusmode.entity.FocusTimerSetting;
import whoreads.backend.domain.focusmode.repository.FocusModeRepository;
import whoreads.backend.domain.member.entity.Member;
import whoreads.backend.domain.member.enums.AgeGroup;
import whoreads.backend.domain.member.enums.Gender;
import whoreads.backend.domain.member.repository.MemberRepository;
import whoreads.backend.domain.readingsession.service.ReadingSessionScheduler;
import whoreads.backend.global.exception.ErrorCode;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * recover API(POST 매핑, 세션 상태별 응답)를 실제 HTTP 흐름으로 검증한다 (H2).
 * 시간 경과는 interval.start_time / last_heartbeat_at 을 과거로 당겨서 흉내낸다.
 */
@SpringBootTest
@Import(TestFirebaseConfig.class)
class ReadingSessionRecoverTest {

    @Autowired WebApplicationContext ctx;
    @Autowired MemberRepository memberRepository;
    @Autowired FocusModeRepository focusModeRepository;
    @Autowired JdbcTemplate jdbc;
    @Autowired ReadingSessionScheduler scheduler;
    ObjectMapper objectMapper = new ObjectMapper();
    MockMvc mockMvc;

    Long memberId;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(ctx).build();
        String u = UUID.randomUUID().toString().substring(0, 8);
        memberId = memberRepository.save(Member.builder()
                .nickname("t" + u).gender(Gender.FEMALE).ageGroup(AgeGroup.TWENTIES)
                .email(u + "@test.com").loginId("id" + u).password("pw").build()).getId();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void recover는_POST_끝_슬래시_없이_호출하면_정상_응답한다() throws Exception {
        long sid = start();
        setHeartbeatMinutesAgo(sid, 0);

        MvcResult r = perform(post("/api/reading-sessions/{id}/recover", sid));

        assertThat(r.getResponse().getStatus()).isEqualTo(200);
    }

    @Test
    void 구버전_GET_recover_끝_슬래시_경로는_POST와_같은_응답을_반환한다() throws Exception {
        long legacySid = start();
        setHeartbeatMinutesAgo(legacySid, 0);
        MvcResult legacy = perform(get("/api/reading-sessions/{id}/recover/", legacySid));
        perform(post("/api/reading-sessions/{id}/complete", legacySid));

        long sid = start();
        setHeartbeatMinutesAgo(sid, 0);
        MvcResult current = perform(post("/api/reading-sessions/{id}/recover", sid));

        assertThat(legacy.getResponse().getStatus()).isEqualTo(200);
        assertThat(body(legacy)).isEqualTo(body(current));
    }

    @Test
    void 구버전_GET_recover로_완료된_세션을_재개하면_409를_반환한다() throws Exception {
        long sid = start();
        perform(post("/api/reading-sessions/{id}/complete", sid));

        MvcResult r = perform(get("/api/reading-sessions/{id}/recover/", sid));

        assertThat(r.getResponse().getStatus()).isEqualTo(409);
        assertThat(r.getResponse().getContentAsString(StandardCharsets.UTF_8))
                .contains("중단된 세션이거나 완료된 세션입니다.");
    }

    @Test
    void recover를_끝_슬래시_없는_GET으로_호출하면_성공하지_않는다() throws Exception {
        long sid = start();
        setHeartbeatMinutesAgo(sid, 0);

        MvcResult r = perform(get("/api/reading-sessions/{id}/recover", sid));

        assertThat(r.getResponse().getStatus()).isGreaterThanOrEqualTo(405);
        assertThat(status(sid)).isEqualTo("IN_PROGRESS");
    }

    @Test
    void 완료된_세션을_recover하면_409를_반환한다() throws Exception {
        long sid = start();
        perform(post("/api/reading-sessions/{id}/complete", sid));
        assertThat(status(sid)).isEqualTo("COMPLETED");

        MvcResult r = perform(post("/api/reading-sessions/{id}/recover", sid));

        assertThat(r.getResponse().getStatus()).isEqualTo(409);
        assertThat(r.getResponse().getContentAsString(StandardCharsets.UTF_8))
                .contains("중단된 세션이거나 완료된 세션입니다.");
    }

    @Test
    void 스케줄러가_중단시킨_세션을_2시간_안에_recover하면_이어서_진행한다() throws Exception {
        focusModeRepository.save(FocusTimerSetting.builder()
                .member(memberRepository.findById(memberId).orElseThrow()).timerMinutes(30L).build());
        long sid = start();
        shiftStartMinutes(sid, 15);
        setHeartbeatMinutesAgo(sid, 10);
        scheduler.expireStaleSessions();
        assertThat(status(sid)).isEqualTo("SUSPENDED");

        MvcResult r = perform(post("/api/reading-sessions/{id}/recover", sid));

        assertThat(r.getResponse().getStatus()).isEqualTo(200);
        assertThat(status(sid)).isEqualTo("IN_PROGRESS");
    }

    @Test
    void 스케줄러가_중단시킨_세션을_2시간_후_recover하면_SESSION_EXPIRED를_반환한다() throws Exception {
        long sid = start();
        shiftStartMinutes(sid, 155);
        setHeartbeatMinutesAgo(sid, 150);
        scheduler.expireStaleSessions();
        assertThat(status(sid)).isEqualTo("SUSPENDED");

        MvcResult r = perform(post("/api/reading-sessions/{id}/recover", sid));

        assertThat(r.getResponse().getStatus()).isEqualTo(400);
        assertThat(r.getResponse().getContentAsString(StandardCharsets.UTF_8))
                .contains(ErrorCode.SESSION_EXPIRED.getMessage());
    }

    // ---- helpers ----

    private long start() throws Exception {
        MvcResult r = perform(post("/api/reading-sessions/start"));
        JsonNode body = objectMapper.readTree(r.getResponse().getContentAsString(StandardCharsets.UTF_8));
        return body.path("result").path("session_id").asLong();
    }

    private MvcResult perform(MockHttpServletRequestBuilder req) throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(memberId, null, List.of()));
        return mockMvc.perform(req).andReturn();
    }

    private JsonNode body(MvcResult r) throws Exception {
        return objectMapper.readTree(r.getResponse().getContentAsString(StandardCharsets.UTF_8));
    }

    private String status(long sid) {
        return jdbc.queryForObject("SELECT status FROM reading_session WHERE id = ?", String.class, sid);
    }

    private void shiftStartMinutes(long sid, int minutes) {
        jdbc.update("UPDATE reading_interval SET start_time = DATEADD('MINUTE', ?, start_time) WHERE session_id = ?", -minutes, sid);
    }

    private void setHeartbeatMinutesAgo(long sid, int minutes) {
        jdbc.update("UPDATE reading_session SET last_heartbeat_at = DATEADD('MINUTE', ?, CURRENT_TIMESTAMP) WHERE id = ?", -minutes, sid);
    }
}
