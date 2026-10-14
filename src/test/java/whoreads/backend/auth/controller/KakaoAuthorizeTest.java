package whoreads.backend.auth.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.util.UriComponentsBuilder;
import whoreads.backend.auth.service.AuthService;
import whoreads.backend.auth.service.EmailService;
import whoreads.backend.auth.service.KakaoOAuthClient;
import whoreads.backend.global.exception.GlobalExceptionHandler;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class KakaoAuthorizeTest {
    private static final String STATE = "a".repeat(43);
    private static final String KEY = "0123456789abcdef0123456789abcdef";
    private static final String CALLBACK = "https://example.invalid/api/auth/kakao/callback";
    private MockMvc mvc;
    private KakaoOAuthClient client;

    @BeforeEach
    void setUp() {
        client = new KakaoOAuthClient();
        ReflectionTestUtils.setField(client, "restApiKey", KEY);
        ReflectionTestUtils.setField(client, "clientSecret", "server-only-dummy-secret");
        ReflectionTestUtils.setField(client, "redirectUri", CALLBACK);
        mvc = MockMvcBuilders.standaloneSetup(new AuthController(
                        mock(AuthService.class), mock(EmailService.class), client))
                .setControllerAdvice(new GlobalExceptionHandler()).build();
    }

    @Test
    void redirectsUsingServerConfigurationWithoutClientSecret() throws Exception {
        var response = mvc.perform(get("/api/auth/kakao/authorize")
                        .param("state", STATE).param("redirect_uri", "https://attacker.invalid"))
                .andExpect(status().isFound())
                .andExpect(header().string("Cache-Control", "no-store"))
                .andExpect(header().string("Referrer-Policy", "no-referrer"))
                .andExpect(content().string(""))
                .andReturn().getResponse();
        var location = URI.create(response.getHeader("Location"));
        var params = UriComponentsBuilder.fromUri(location).build().getQueryParams();
        assertThat(location.getHost()).isEqualTo("kauth.kakao.com");
        assertThat(location.getPath()).isEqualTo("/oauth/authorize");
        assertThat(params.keySet()).containsExactlyInAnyOrder("client_id", "redirect_uri", "response_type", "state");
        assertThat(params.getFirst("client_id")).isEqualTo(KEY);
        assertThat(params.getFirst("state")).isEqualTo(STATE);
        assertThat(java.net.URLDecoder.decode(params.getFirst("redirect_uri"), java.nio.charset.StandardCharsets.UTF_8))
                .isEqualTo(CALLBACK);
        assertThat(location.toString()).doesNotContain("server-only-dummy-secret", "client_secret", "attacker.invalid");
    }

    @Test
    void rejectsMissingDuplicateShortAndInjectedState() throws Exception {
        mvc.perform(get("/api/auth/kakao/authorize")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/auth/kakao/authorize").param("state", STATE, STATE))
                .andExpect(status().isBadRequest()).andExpect(header().doesNotExist("Location"));
        for (String invalid : new String[]{"short", "a".repeat(129), STATE + "&client_secret=bad", STATE + "\r\n"}) {
            mvc.perform(get("/api/auth/kakao/authorize").param("state", invalid))
                    .andExpect(status().isBadRequest()).andExpect(header().doesNotExist("Location"));
        }
    }

    @Test
    void refusesUnsafeServerCallbackOrMissingServerKey() throws Exception {
        for (String invalid : new String[]{"http://example.invalid/api/auth/kakao/callback",
                "https://user@example.invalid/api/auth/kakao/callback",
                CALLBACK + "?next=attacker", CALLBACK + "#fragment", "https://example.invalid/other"}) {
            ReflectionTestUtils.setField(client, "redirectUri", invalid);
            mvc.perform(get("/api/auth/kakao/authorize").param("state", STATE))
                    .andExpect(status().isBadGateway()).andExpect(header().doesNotExist("Location"));
        }
        ReflectionTestUtils.setField(client, "redirectUri", CALLBACK);
        ReflectionTestUtils.setField(client, "restApiKey", "");
        mvc.perform(get("/api/auth/kakao/authorize").param("state", STATE))
                .andExpect(status().isBadGateway()).andExpect(header().doesNotExist("Location"));
    }
}
