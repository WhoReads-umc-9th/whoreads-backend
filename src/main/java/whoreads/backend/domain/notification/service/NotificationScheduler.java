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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
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

    @Scheduled(cron = "0 * * * * *", zone = "Asia/Seoul")
    public void checkRoutineNotifications() {

        ZoneId zone = ZoneId.of("Asia/Seoul");
        LocalDateTime now = LocalDateTime.now(zone);

        LocalDate date = now.toLocalDate();
        String day = now.getDayOfWeek().name();

        String time = now.toLocalTime()
                .format(DateTimeFormatter.ofPattern("HH:mm"));

        List<MemberTokenDTO> tokens =
                notificationSettingRepository
                        .findMemberIdAndTokenByDayAndTime(day, time);

        if (tokens.isEmpty()) {
            return;
        }

        String idempotencyKey =
                "ROUTINE:" + date + ":" + day + ":" + time;

        applicationEventPublisher.publishEvent(
                new NotificationEvent.RoutineEvent(
                        tokens,
                        idempotencyKey
                )
        );
    }
    @Transactional
    @Scheduled(cron = "0 0 0 * * *",zone = "Asia/Seoul") // 매일 자정 실행
    public void deleteOldNotifications() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(7);
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
