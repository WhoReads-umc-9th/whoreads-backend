package whoreads.backend.domain.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.types.Expiration;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationIdempotencyService {

    private final StringRedisTemplate redisTemplate;

    private static final String PREFIX = "notification:idempotency:";

    // 발송 도중 워커가 종료돼도 키가 TTL 끝까지 남아 재전송이 막히지 않도록, 처리 중 키는 짧은 임대 TTL만 갖는다.
    // 발송 완료 시간보다 충분히 길어야 한다. 임대가 발송 중에 만료되면 다른 워커가 같은 알림을 다시 보낼 수 있다.
    static final Duration PROCESSING_LEASE = Duration.ofMinutes(5);

    // 키가 없거나 FAILED(발송이 확실히 실패한 상태)면 PROCESSING 임대로 선점한다. PROCESSING, SENT는 차단한다.
    private static final DefaultRedisScript<Long> ACQUIRE_SCRIPT = new DefaultRedisScript<>("""
            local current = redis.call('GET', KEYS[1])
            if not current or current == 'FAILED' then
                redis.call('SET', KEYS[1], 'PROCESSING', 'PX', ARGV[1])
                return 1
            end
            return 0
            """, Long.class);

    public boolean acquire(String key, Duration ttl) {
        Duration lease = ttl.compareTo(PROCESSING_LEASE) < 0 ? ttl : PROCESSING_LEASE;

        Long acquired = redisTemplate.execute(
                ACQUIRE_SCRIPT,
                List.of(PREFIX + key),
                String.valueOf(lease.toMillis())
        );

        return Long.valueOf(1L).equals(acquired);
    }

    // 발송이 완료된 키는 임대가 아니라 전체 TTL 동안 중복 발송을 막는다.
    public void markSent(String key, Duration ttl) {
        redisTemplate.opsForValue().set(PREFIX + key, "SENT", ttl);
    }

    // FAILED는 재시도 가능 상태다. 남은 임대 TTL이 지나면 키가 사라져 어느 경우든 다시 선점할 수 있다.
    public void markFailed(String key, String reason) {
        String redisKey = PREFIX + key;

        // 기존 키의 TTL을 유지하면서 상태만 갱신 (SET XX KEEPTTL)
        redisTemplate.execute((RedisCallback<Boolean>) connection ->
                connection.stringCommands().set(
                        redisKey.getBytes(java.nio.charset.StandardCharsets.UTF_8),
                        "FAILED".getBytes(java.nio.charset.StandardCharsets.UTF_8),
                        Expiration.keepTtl(),
                        RedisStringCommands.SetOption.ifPresent()
                )
        );
        log.error("[Notification] 발송 실패. key={}, reason={}", key, reason);
    }
}
