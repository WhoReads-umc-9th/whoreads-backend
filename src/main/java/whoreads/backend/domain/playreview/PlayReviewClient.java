package whoreads.backend.domain.playreview;

import com.google.auth.oauth2.GoogleCredentials;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.util.*;

@Component
@ConditionalOnProperty(name = "play-review-monitor.enabled", havingValue = "true")
public class PlayReviewClient {
    static final String PLAY_URL = "https://androidpublisher.googleapis.com/androidpublisher/v3/applications/com.whoreads.mobile/tracks/alpha/releases";
    public static final String MESSAGE = "[WhoReads] - android 비공개 테스트 심사완료";
    public static final String PROBE_MESSAGE = "[WhoReads] - android 비공개 테스트 알림 연결 테스트 (백엔드 스케줄러)";
    private final GoogleCredentials credentials;
    private final String webhook;
    private final RestTemplate http;

    public PlayReviewClient(@Value("${play-review-monitor.service-account-base64}") String account,
                            @Value("${play-review-monitor.discord-webhook}") String webhook) throws Exception {
        if (!webhook.matches("https://discord\\.com/api/webhooks/\\d+/[A-Za-z0-9_-]+"))
            throw new IllegalArgumentException("Invalid Discord webhook configuration");
        this.webhook = webhook;
        this.credentials = GoogleCredentials.fromStream(new ByteArrayInputStream(Base64.getDecoder().decode(account)))
                .createScoped("https://www.googleapis.com/auth/androidpublisher");
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10_000);
        factory.setReadTimeout(20_000);
        this.http = new RestTemplate(factory);
    }

    private synchronized String token() {
        try {
            credentials.refreshIfExpired();
            return credentials.getAccessToken().getTokenValue();
        } catch (Exception ignored) {
            throw new IllegalStateException("play_auth_failed");
        }
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> releases() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token());
        Map<String, Object> response = call(PLAY_URL, HttpMethod.GET, new HttpEntity<>(headers), "play");
        Object releases = response.getOrDefault("releases", List.of());
        if (!(releases instanceof List<?>)) throw new IllegalStateException("play_invalid_response");
        return (List<Map<String, Object>>) releases;
    }

    @SuppressWarnings("unchecked")
    public String lifecycle(long code) {
        boolean newer = false;
        for (Map<String, Object> release : releases()) {
            if (!"alpha".equals(release.get("track"))) throw new IllegalStateException("play_unexpected_track");
            for (Map<String, Object> artifact : (List<Map<String, Object>>) release.getOrDefault("activeArtifacts", List.of())) {
                long version = ((Number) artifact.get("versionCode")).longValue();
                if (version == code) return Objects.toString(release.get("releaseLifecycleState"), "UNKNOWN");
                newer |= version > code;
            }
        }
        return newer ? "SUPERSEDED" : "NOT_VISIBLE";
    }

    public String notify(boolean probe) {
        String message = probe ? PROBE_MESSAGE : MESSAGE;
        Map<String, Object> result = call(webhook + "?wait=true", HttpMethod.POST,
                new HttpEntity<>(Map.of("content", message, "allowed_mentions", Map.of("parse", List.of()))), "discord");
        String id = Objects.toString(result.get("id"), "");
        if (!id.matches("\\d+") || !message.equals(result.get("content")))
            throw new IllegalStateException("discord_delivery_unconfirmed");
        return id;
    }

    public Map<String, Object> health() {
        int count = releases().size();
        Map<String, Object> discord = call(webhook, HttpMethod.GET, HttpEntity.EMPTY, "discord");
        return Map.of("play_connected", true, "alpha_releases", count,
                "discord_connected", discord.containsKey("id"));
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> call(String url, HttpMethod method, HttpEntity<?> body, String source) {
        try {
            var response = http.exchange(URI.create(url), method, body, Map.class).getBody();
            if (response == null) throw new IllegalStateException(source + "_empty_response");
            return response;
        } catch (RestClientResponseException error) {
            // HTTP exceptions can embed secret webhook URLs, tokens and response bodies.
            throw new IllegalStateException(source + "_http_" + error.getStatusCode().value());
        } catch (RestClientException ignored) {
            throw new IllegalStateException(source + "_connection_failed");
        }
    }
}
