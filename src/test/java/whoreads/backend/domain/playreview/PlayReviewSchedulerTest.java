package whoreads.backend.domain.playreview;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.*;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class PlayReviewSchedulerTest {
    private static final String SHA = "a".repeat(40);
    private static final String REVIEW = "RELEASE_LIFECYCLE_STATE_IN_REVIEW";
    private static final String APPROVED = "RELEASE_LIFECYCLE_STATE_APPROVED_NOT_PUBLISHED";
    private static final String PUBLISHED = "RELEASE_LIFECYCLE_STATE_PUBLISHED";
    private JdbcTemplate jdbc;
    private PlayReviewStore store;
    private PlayReviewClient client;
    private PlayReviewScheduler scheduler;
    private MutableClock clock;

    static class MutableClock extends Clock {
        Instant time = Instant.parse("2026-10-15T12:00:00Z");
        public ZoneId getZone() { return ZoneOffset.UTC; }
        public Clock withZone(ZoneId zone) { return this; }
        public Instant instant() { return time; }
        void advance(Duration duration) { time = time.plus(duration); }
    }

    @BeforeEach
    void setup() throws Exception {
        var source = new DriverManagerDataSource("jdbc:h2:mem:" + UUID.randomUUID() + ";MODE=MySQL;DB_CLOSE_DELAY=-1", "sa", "");
        jdbc = new JdbcTemplate(source);
        jdbc.execute(Files.readString(Path.of("src/main/resources/db/migration/V2__play_review_monitor.sql")));
        clock = new MutableClock();
        store = new PlayReviewStore(jdbc, new TransactionTemplate(new DataSourceTransactionManager(source)), clock);
        client = mock(PlayReviewClient.class);
        scheduler = new PlayReviewScheduler(store, client);
    }

    @Test
    void idleSchedulerDoesNotCallPlayOrDiscord() {
        scheduler.tick();
        verifyNoInteractions(client);
    }

    @Test
    void submissionStartsPollingAndChecksAgainAfterTenMinutes() {
        store.register(6, SHA, false, null);
        when(client.lifecycle(6)).thenReturn(REVIEW);
        scheduler.tick();
        scheduler.tick();
        verify(client, times(1)).lifecycle(6);
        assertThat(store.status("release-6").get("state")).isEqualTo("ACTIVE");
        clock.advance(Duration.ofMinutes(10));
        scheduler.tick();
        verify(client, times(2)).lifecycle(6);
        verify(client, never()).notify(anyBoolean());
    }

    @Test
    void approvalSendsOnceAndEndsPollingEvenAfterReRegistrationAndPublication() {
        store.register(6, SHA, false, null);
        when(client.lifecycle(6)).thenReturn(APPROVED);
        when(client.notify(false)).thenReturn("1560272263427653683");
        scheduler.tick();
        assertThat(store.status("release-6").get("state")).isEqualTo("SENT");
        store.register(6, SHA, false, null);
        doReturn(PUBLISHED).when(client).lifecycle(6);
        clock.advance(Duration.ofDays(1));
        scheduler.tick();
        verify(client, times(1)).lifecycle(6);
        verify(client, times(1)).notify(false);
        assertThat(store.pending()).isZero();
    }

    @Test
    void checkingLeaseSurvivesRestartAndOnlyOneInstanceCanClaim() {
        store.register(6, SHA, false, null);
        assertThat(store.claim()).isNotNull();
        var restarted = new PlayReviewScheduler(store, client);
        restarted.tick();
        verifyNoInteractions(client);
        clock.advance(Duration.ofMinutes(4));
        when(client.lifecycle(6)).thenReturn(REVIEW);
        restarted.tick();
        verify(client).lifecycle(6);
    }

    @Test
    void staleWorkerCannotStartSendingAfterItsLeaseWasReclaimed() {
        store.register(6, SHA, false, null);
        var old = store.claim();
        clock.advance(Duration.ofMinutes(4));
        var current = store.claim();
        assertThat(store.sending(old, PUBLISHED)).isFalse();
        assertThat(store.sending(current, PUBLISHED)).isTrue();
    }

    @Test
    void ambiguousDeliveryDoesNotRetryOrSendTwice() {
        store.register(6, SHA, false, null);
        when(client.lifecycle(6)).thenReturn(PUBLISHED);
        when(client.notify(false)).thenThrow(new IllegalStateException("discord_connection_failed"));
        scheduler.tick();
        clock.advance(Duration.ofDays(1));
        scheduler.tick();
        verify(client, times(1)).notify(false);
        assertThat(store.status("release-6").get("state")).isEqualTo("DELIVERY_UNCERTAIN");
    }

    @Test
    void sendingCrashBecomesUncertainInsteadOfSendingAgain() {
        store.register(6, SHA, false, null);
        var job = store.claim();
        assertThat(store.sending(job, PUBLISHED)).isTrue();
        clock.advance(Duration.ofMinutes(4));
        scheduler.tick();
        verifyNoInteractions(client);
        assertThat(store.status("release-6").get("state")).isEqualTo("DELIVERY_UNCERTAIN");
    }

    @Test
    void transientPlayFailureRetriesAndNeverSendsPrematurely() {
        store.register(6, SHA, false, null);
        when(client.lifecycle(6)).thenThrow(new IllegalStateException("play_http_503"));
        scheduler.tick();
        verify(client, never()).notify(anyBoolean());
        assertThat(store.status("release-6").get("last_error")).isEqualTo("play_check_failed");
        clock.advance(Duration.ofMinutes(10));
        doReturn(PUBLISHED).when(client).lifecycle(6);
        when(client.notify(false)).thenReturn("1560272263427653683");
        scheduler.tick();
        assertThat(store.status("release-6").get("state")).isEqualTo("SENT");
    }

    @Test
    void rejectedAndSupersededReleasesStopWithoutSuccessMessage() {
        for (String state : new String[]{"RELEASE_LIFECYCLE_STATE_NOT_APPROVED", "SUPERSEDED"}) {
            long code = state.equals("SUPERSEDED") ? 7 : 6;
            store.register(code, SHA, false, null);
            when(client.lifecycle(code)).thenReturn(state);
        }
        scheduler.tick();
        verify(client, never()).notify(anyBoolean());
        assertThat(store.pending()).isZero();
    }

    @Test
    void oldDeliveryReceiptIsImportedWithoutPostingAgain() {
        store.register(5, SHA, false, "1560272263427653683");
        store.register(5, SHA, false, null);
        scheduler.tick();
        verifyNoInteractions(client);
        assertThat(store.status("release-5").get("discord_message_id")).isEqualTo("1560272263427653683");
    }

    @Test
    void liveProbeUsesExplicitTestMessageAndDoesNotCompleteReleaseJob() {
        Map<String, Object> probe = store.register(5, SHA, true, null);
        when(client.lifecycle(5)).thenReturn(PUBLISHED);
        when(client.notify(true)).thenReturn("1560272263427653684");
        scheduler.tick();
        verify(client).notify(true);
        verify(client, never()).notify(false);
        assertThat(store.status((String) probe.get("job_key")).get("state")).isEqualTo("SENT");
        assertThat(store.status("release-5").get("state")).isEqualTo("NOT_REGISTERED");
    }
}
