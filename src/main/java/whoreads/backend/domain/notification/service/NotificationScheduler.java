package whoreads.backend.domain.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import whoreads.backend.domain.notification.event.NotificationEvent;
import whoreads.backend.domain.notification.repository.NotificationRepository;
import whoreads.backend.global.exception.CustomException;
import whoreads.backend.global.exception.ErrorCode;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationScheduler {
    private final NotificationRepository notificationRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Scheduled(cron = "0 * * * * *",zone = "Asia/Seoul")
    public void checkRoutineNotifications() {
        String currentDay = LocalDate.now().getDayOfWeek().name();
        String currentTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));

        // 현재 알림 관련 텍스트는 따로 처리가 필요없고 토큰만 가져오면 됨
        List<String> tokens = notificationRepository.findAllTokensByDayAndTime(currentDay,currentTime);
        log.info("[디버깅] 현재 요일: {}, 현재 시간: {}", currentDay, currentTime);
        log.info("[디버깅] 검색된 루틴 개수: {}", tokens.size());

        if (tokens.isEmpty())
            return;
        try {
            // 루틴 이벤트 발행
            applicationEventPublisher.publishEvent
                    (new NotificationEvent.RoutineEvent(tokens));
        } catch (Exception e) {
            log.error("알림 발송 실패 : {}건", tokens.size());
            throw new CustomException(ErrorCode.FCM_SEND_FAILED);
        }
    }
}
