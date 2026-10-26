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
        // 5 -> 2로 수정
        // LocalDateTime threshold = LocalDateTime.now().minusHours(2);

        // 테스트용
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(5);
        List<ReadingSession> staleSessions = readingSessionRepository.findStaleInProgressSessions(threshold);

        if (staleSessions.isEmpty()) {
            return;
        }

        for (ReadingSession session : staleSessions) {
            session.getIntervals().stream()
                    .filter(interval -> interval.getEndTime() == null)
                    .findFirst()
//                    .ifPresent(interval -> interval.end(session.getLastHeartbeatAt()));

                    .ifPresent(interval -> {
                        LocalDateTime endTime = session.getLastHeartbeatAt();

                        // 하트비트가 아예 없거나, 인터벌 시작 시간보다 과거/동일한 경우
                        if (endTime == null || !endTime.isAfter(interval.getStartTime())) {
                            // 에러(IllegalArgumentException)가 나지 않도록 시작 시간보다 1초 뒤로 강제 설정
                            endTime = interval.getStartTime().plusSeconds(1);
                        }

                        // 안전하게 검증된 시간으로 인터벌 종료
                        interval.end(endTime);
                    });

            // 사용자의 타이머 설정값(timerMinutes) 가져오기
            Long userGoalTime = 0L;
            if (session.getMember().getFocusTimerSetting() != null) {
                userGoalTime = session.getMember().getFocusTimerSetting().getTimerMinutes();
            }

            // 3. 중단 처리 및 시간 계산 실행
            session.suspend(userGoalTime);
        }

        log.info("[ReadingSessionScheduler] stale 세션 {}건 자동 완료 처리", staleSessions.size());
    }

    // 일시정지한 지 2시간이 지난 세션은 이어서 읽는 것이 아니므로 일시정지한 시점까지의 시간으로 완료
    // (완료된 세션만 통계·기록에 집계되므로, 방치된 일시정지 세션이 기록에서 빠지지 않도록)
    @Transactional
    @Scheduled(fixedDelay = 60_000, zone = "Asia/Seoul")
    public void completeAbandonedPausedSessions() {
        LocalDateTime threshold = LocalDateTime.now().minusHours(2);
        List<ReadingSession> pausedSessions = readingSessionRepository.findPausedSessionsPausedBefore(threshold);

        if (pausedSessions.isEmpty()) {
            return;
        }

        pausedSessions.forEach(ReadingSession::completeLeftover);

        log.info("[ReadingSessionScheduler] 일시정지 2시간 경과 세션 {}건 자동 완료 처리", pausedSessions.size());
    }
}
