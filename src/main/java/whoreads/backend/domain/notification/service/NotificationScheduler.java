package whoreads.backend.domain.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import whoreads.backend.domain.member.repository.MemberRepository;
import whoreads.backend.domain.notification.dto.MemberTokenDTO;
import whoreads.backend.domain.notification.event.NotificationEvent;
import whoreads.backend.domain.notification.repository.NotificationHistoryRepository;
import whoreads.backend.domain.notification.repository.NotificationSettingRepository;
import whoreads.backend.global.exception.CustomException;
import whoreads.backend.global.exception.ErrorCode;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationScheduler {
    private final NotificationSettingRepository notificationSettingRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final NotificationHistoryRepository notificationHistoryRepository;
    private final MemberRepository memberRepository;

    @Scheduled(cron = "0 * * * * *",zone = "Asia/Seoul")
    public void checkRoutineNotifications() {
        String currentDay = LocalDate.now().getDayOfWeek().name();
        String currentTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));

        // 현재 알림 관련 텍스트는 따로 처리가 필요없고 토큰만 가져오면 됨
        List<MemberTokenDTO> tokens = notificationSettingRepository.findMemberIdAndTokenByDayAndTime(currentDay,currentTime);
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
    @Transactional
    @Scheduled(cron = "0 0 0 * * *",zone = "Asia/Seoul") // 매일 자정 실행
    public void deleteOldNotifications() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(30);
        notificationHistoryRepository.deleteByCreatedAtBefore(cutoff);
    }
    /*
     * 매일 2시 30일 지난 토큰 정리
     * */
    @Scheduled(cron = "0 0 2 * * *",zone = "Asia/Seoul")
    @Transactional
    public void deleteInactiveUsers() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(30);
        memberRepository.clearExpiredTokens(threshold);
        log.info("미접속자 토큰 제거 완료");
    }
}
