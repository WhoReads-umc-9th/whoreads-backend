package whoreads.backend.domain.readingsession.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import whoreads.backend.domain.readingsession.entity.ReadingSession;
import whoreads.backend.domain.readingsession.repository.ReadingSessionRepository;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReadingSessionScheduler {

    private final ReadingSessionRepository readingSessionRepository;

    @Transactional
    @Scheduled(fixedDelay = 60_000, zone = "Asia/Seoul")
    public void expireStaleSessions() {
        LocalDateTime threshold = LocalDateTime.now().minusHours(5);
        List<ReadingSession> staleSessions = readingSessionRepository.findStaleInProgressSessions(threshold);

        if (staleSessions.isEmpty()) {
            return;
        }

        for (ReadingSession session : staleSessions) {
            session.getIntervals().stream()
                    .filter(interval -> interval.getEndTime() == null)
                    .findFirst()
                    .ifPresent(interval -> interval.end(session.getLastHeartbeatAt()));
            session.complete();
        }

        log.info("[ReadingSessionScheduler] stale 세션 {}건 자동 완료 처리", staleSessions.size());
    }
}
