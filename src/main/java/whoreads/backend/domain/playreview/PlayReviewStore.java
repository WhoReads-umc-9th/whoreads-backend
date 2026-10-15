package whoreads.backend.domain.playreview;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.Timestamp;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** DB leases survive restarts and prevent both blue/green instances from sending. */
@Component
public class PlayReviewStore {
    private final JdbcTemplate jdbc;
    private final TransactionTemplate transactions;
    private final Clock clock;

    public record Job(String key, long versionCode, String claimToken, int attempts) {
        public boolean probe() { return key.startsWith("probe-"); }
    }

    @org.springframework.beans.factory.annotation.Autowired
    public PlayReviewStore(JdbcTemplate jdbc, org.springframework.transaction.PlatformTransactionManager manager) {
        this(jdbc, new TransactionTemplate(manager), Clock.systemUTC());
    }

    PlayReviewStore(JdbcTemplate jdbc, TransactionTemplate transactions, Clock clock) {
        this.jdbc = jdbc;
        this.transactions = transactions;
        this.clock = clock;
    }

    private Timestamp now() { return Timestamp.from(clock.instant()); }
    private Timestamp after(Duration duration) { return Timestamp.from(clock.instant().plus(duration)); }

    public Map<String, Object> register(long code, String commit, boolean probe, String priorMessageId) {
        String key = probe ? "probe-" + UUID.randomUUID() : "release-" + code;
        String state = priorMessageId == null ? "ACTIVE" : "SENT";
        try {
            jdbc.update("""
                INSERT INTO play_review_job
                (job_key, version_code, source_commit, state, next_check_at, discord_message_id, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """, key, code, commit, state, now(), priorMessageId, now(), now());
        } catch (DuplicateKeyException ignored) {
            // Retrying registration never resets a completed job or its due time.
        }
        return status(key);
    }

    public Map<String, Object> status(String key) {
        List<Map<String, Object>> rows = jdbc.queryForList("""
            SELECT job_key, version_code, state, last_lifecycle, next_check_at, attempts,
                   last_error, discord_message_id, updated_at
            FROM play_review_job WHERE job_key = ?
            """, key);
        if (rows.isEmpty()) return Map.of("state", "NOT_REGISTERED");
        // Explicit lowercase keys keep the API consistent across MySQL and H2.
        var result = new java.util.LinkedHashMap<String, Object>();
        rows.getFirst().forEach((name, value) -> result.put(name.toLowerCase(java.util.Locale.ROOT),
                value instanceof Timestamp time ? time.toInstant().toString() : value));
        return result;
    }

    public Job claim() {
        return transactions.execute(ignored -> {
            // A crash while sending is ambiguous: do not post the same message again.
            jdbc.update("""
                UPDATE play_review_job SET state='DELIVERY_UNCERTAIN', last_error='delivery_requires_reconciliation', updated_at=?
                WHERE state='SENDING' AND lease_until < ?
                """, now(), now());
            List<Job> jobs = jdbc.query("""
                SELECT job_key, version_code, attempts FROM play_review_job
                WHERE (state='ACTIVE' AND next_check_at <= ?) OR (state='CHECKING' AND lease_until < ?)
                ORDER BY next_check_at LIMIT 1 FOR UPDATE
                """, (rs, row) -> new Job(rs.getString(1), rs.getLong(2), UUID.randomUUID().toString(), rs.getInt(3) + 1), now(), now());
            if (jobs.isEmpty()) return null;
            Job job = jobs.getFirst();
            jdbc.update("""
                UPDATE play_review_job SET state='CHECKING', claim_token=?, lease_until=?, attempts=?, updated_at=?
                WHERE job_key=?
                """, job.claimToken(), after(Duration.ofMinutes(3)), job.attempts(), now(), job.key());
            return job;
        });
    }

    public boolean sending(Job job, String lifecycle) {
        return jdbc.update("""
            UPDATE play_review_job SET state='SENDING', last_lifecycle=?, updated_at=?
            WHERE job_key=? AND state='CHECKING' AND claim_token=?
            """, lifecycle, now(), job.key(), job.claimToken()) == 1;
    }

    public void finish(Job job, String state, String lifecycle, String messageId, String error) {
        jdbc.update("""
            UPDATE play_review_job SET state=?, last_lifecycle=?, discord_message_id=?, last_error=?, updated_at=?,
                next_check_at=?, lease_until=NULL
            WHERE job_key=? AND claim_token=? AND state IN ('CHECKING','SENDING')
            """, state, lifecycle, messageId, error, now(), after(Duration.ofMinutes(10)), job.key(), job.claimToken());
    }

    public int pending() {
        return jdbc.queryForObject("SELECT COUNT(*) FROM play_review_job WHERE state IN ('ACTIVE','CHECKING','SENDING')", Integer.class);
    }
}
