package whoreads.backend.domain.playreview;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.util.Map;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class PlayReviewControllerTest {
    private final String token = "a".repeat(64);
    private final String body = "{\"version_code\":6,\"commit\":\"" + "b".repeat(40) + "\"}";
    private PlayReviewStore store;
    private PlayReviewClient client;
    private MockMvc mvc;

    @BeforeEach
    void setup() {
        store = mock(PlayReviewStore.class);
        client = mock(PlayReviewClient.class);
        mvc = MockMvcBuilders.standaloneSetup(new PlayReviewController(store, client, token)).build();
    }

    @Test
    void missingOrWrongTokenCannotRegisterOrReadStatus() throws Exception {
        for (String value : new String[]{"", "wrong"}) {
            mvc.perform(post("/api/internal/play-review/jobs").header("X-Play-Review-Token", value)
                    .contentType("application/json").content(body)).andExpect(status().isUnauthorized());
            mvc.perform(get("/api/internal/play-review/health").header("X-Play-Review-Token", value))
                    .andExpect(status().isUnauthorized());
        }
        verifyNoInteractions(store, client);
    }

    @Test
    void authorizedSubmissionRegistersOnlyItsVersion() throws Exception {
        when(store.register(6, "b".repeat(40), false, null)).thenReturn(Map.of("state", "ACTIVE", "job_key", "release-6"));
        mvc.perform(post("/api/internal/play-review/jobs").header("X-Play-Review-Token", token)
                .contentType("application/json").content(body)).andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value("ACTIVE"));
        verify(store).register(6, "b".repeat(40), false, null);
    }

    @Test
    void invalidVersionAndCommitCannotCreateJobs() throws Exception {
        for (String invalid : new String[]{body.replace(":6,", ":0,"), body.replace(":6,", ":2100000001,"),
                body.replace(":6,", ":6.5,"), body.replace("b".repeat(40), "bad")}) {
            mvc.perform(post("/api/internal/play-review/jobs").header("X-Play-Review-Token", token)
                    .contentType("application/json").content(invalid)).andExpect(status().isBadRequest());
        }
        verifyNoInteractions(store, client);
    }
}
