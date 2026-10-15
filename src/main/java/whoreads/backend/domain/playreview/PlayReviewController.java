package whoreads.backend.domain.playreview;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Map;

@RestController
@RequestMapping("/api/internal/play-review")
@ConditionalOnProperty(name = "play-review-monitor.enabled", havingValue = "true")
public class PlayReviewController {
    private final PlayReviewStore store;
    private final PlayReviewClient client;
    private final byte[] token;

    public PlayReviewController(PlayReviewStore store, PlayReviewClient client,
                                @Value("${play-review-monitor.token}") String token) {
        if (token.length() < 43) throw new IllegalArgumentException("Review monitor token must have at least 256 bits of entropy");
        this.store = store;
        this.client = client;
        this.token = token.getBytes(StandardCharsets.UTF_8);
    }

    private boolean authorized(String supplied) {
        return supplied != null && MessageDigest.isEqual(token, supplied.getBytes(StandardCharsets.UTF_8));
    }

    @PostMapping({"/jobs", "/probes"})
    public ResponseEntity<?> register(@RequestHeader(value = "X-Play-Review-Token", required = false) String supplied,
                                      @RequestBody Map<String, Object> body, jakarta.servlet.http.HttpServletRequest request) {
        if (!authorized(supplied)) return ResponseEntity.status(401).body(Map.of("error", "unauthorized"));
        String codeText = String.valueOf(body.get("version_code"));
        String commit = String.valueOf(body.get("commit"));
        String prior = body.containsKey("prior_message_id") ? String.valueOf(body.get("prior_message_id")) : null;
        if (!codeText.matches("[1-9][0-9]{0,9}") || Long.parseLong(codeText) > 2_100_000_000L
                || !commit.matches("[0-9a-f]{40}") || (prior != null && !prior.matches("[0-9]{10,32}")))
            return ResponseEntity.badRequest().body(Map.of("error", "invalid_release"));
        boolean probe = request.getRequestURI().endsWith("/probes");
        if (probe && prior != null) return ResponseEntity.badRequest().body(Map.of("error", "invalid_probe"));
        return ResponseEntity.ok(store.register(Long.parseLong(codeText), commit, probe, prior));
    }

    @GetMapping("/jobs/{key}")
    public ResponseEntity<?> status(@RequestHeader(value = "X-Play-Review-Token", required = false) String supplied,
                                    @PathVariable String key) {
        if (!authorized(supplied)) return ResponseEntity.status(401).body(Map.of("error", "unauthorized"));
        return ResponseEntity.ok(store.status(key));
    }

    @GetMapping("/health")
    public ResponseEntity<?> health(@RequestHeader(value = "X-Play-Review-Token", required = false) String supplied) {
        if (!authorized(supplied)) return ResponseEntity.status(401).body(Map.of("error", "unauthorized"));
        var result = new java.util.LinkedHashMap<>(client.health());
        result.put("pending_jobs", store.pending());
        result.put("message", PlayReviewClient.MESSAGE);
        return ResponseEntity.ok(result);
    }
}
