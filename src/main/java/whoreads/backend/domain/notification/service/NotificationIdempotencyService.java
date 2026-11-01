package whoreads.backend.domain.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.types.Expiration;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationIdempotencyService {

    private final StringRedisTemplate redisTemplate;

    private static final String PREFIX = "notification:idempotency:";

    public boolean acquire(String key, Duration ttl) {
        Boolean acquired = redisTemplate.opsForValue()
                .setIfAbsent(PREFIX + key, "PROCESSING", ttl);

        return Boolean.TRUE.equals(acquired);
    }

    public void markSent(String key) {
        updateStatus(key, "SENT");
    }

    public void markFailed(String key, String reason) {
        updateStatus(key, "FAILED");
        log.error("[Notification] 발송 실패. key={}, reason={}", key, reason);
    }

    private void updateStatus(String key, String status) {
        String redisKey = PREFIX + key;

        // 기존 키의 TTL을 유지하면서 상태만 갱신 (SET XX KEEPTTL)
        redisTemplate.execute((RedisCallback<Boolean>) connection ->
                connection.stringCommands().set(
                        redisKey.getBytes(java.nio.charset.StandardCharsets.UTF_8),
                        status.getBytes(java.nio.charset.StandardCharsets.UTF_8),
                        Expiration.keepTtl(),
                        RedisStringCommands.SetOption.ifPresent()
                )
        );
    }
}