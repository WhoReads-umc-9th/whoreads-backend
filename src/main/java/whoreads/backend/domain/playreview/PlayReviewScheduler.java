package whoreads.backend.domain.playreview;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(name = "play-review-monitor.enabled", havingValue = "true")
public class PlayReviewScheduler {
    private final PlayReviewStore store;
    private final PlayReviewClient client;

    public PlayReviewScheduler(PlayReviewStore store, PlayReviewClient client) {
        this.store = store;
        this.client = client;
    }

    @Configuration
    @ConditionalOnProperty(name = "play-review-monitor.enabled", havingValue = "true")
    static class Scheduling {
        @Bean
        ThreadPoolTaskScheduler playReviewTaskScheduler() {
            var scheduler = new ThreadPoolTaskScheduler();
            scheduler.setPoolSize(1);
            scheduler.setThreadNamePrefix("play-review-");
            scheduler.setRemoveOnCancelPolicy(true);
            return scheduler;
        }
    }

    // A dedicated worker avoids blocking the existing reading and notification timers.
    // An empty queue makes no Google or Discord requests.
    @Scheduled(fixedDelayString = "${play-review-monitor.tick-ms:15000}", initialDelay = 10000,
            scheduler = "playReviewTaskScheduler")
    public void tick() {
        for (int count = 0; count < 10; count++) {
            PlayReviewStore.Job job = store.claim();
            if (job == null) return;
            check(job);
        }
    }

    private void check(PlayReviewStore.Job job) {
        String lifecycle;
        try {
            lifecycle = client.lifecycle(job.versionCode());
        } catch (Exception error) {
            store.finish(job, "ACTIVE", null, null, "play_check_failed");
            log.warn("Play review lookup failed for versionCode {}; retry scheduled", job.versionCode());
            return;
        }
        if ("RELEASE_LIFECYCLE_STATE_APPROVED_NOT_PUBLISHED".equals(lifecycle)
                || "RELEASE_LIFECYCLE_STATE_PUBLISHED".equals(lifecycle)) {
            if (!store.sending(job, lifecycle)) return;
            try {
                String id = client.notify(job.probe());
                store.finish(job, "SENT", lifecycle, id, null);
                log.info("Play review {} notification confirmed: versionCode {}, message {}",
                        job.probe() ? "probe" : "completion", job.versionCode(), id);
            } catch (Exception error) {
                store.finish(job, "DELIVERY_UNCERTAIN", lifecycle, null, "delivery_requires_reconciliation");
                log.error("Play review Discord delivery needs reconciliation: versionCode {}", job.versionCode());
            }
        } else {
            String state = switch (lifecycle) {
                case "RELEASE_LIFECYCLE_STATE_NOT_APPROVED" -> "REJECTED";
                case "SUPERSEDED" -> "SUPERSEDED";
                default -> "ACTIVE";
            };
            store.finish(job, state, lifecycle, null, null);
        }
    }
}
