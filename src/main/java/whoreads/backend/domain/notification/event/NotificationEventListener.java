package whoreads.backend.domain.notification.event;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import whoreads.backend.domain.member.repository.MemberCelebrityRepository;
import whoreads.backend.domain.member.repository.MemberRepository;
import whoreads.backend.domain.notification.dto.FcmMessageDTO;
import whoreads.backend.domain.notification.dto.MemberToken;
import whoreads.backend.domain.notification.dto.MemberTokenDTO;
import whoreads.backend.domain.notification.enums.NotificationType;
import whoreads.backend.domain.notification.service.NotificationPushService;

import java.time.Duration;
import java.util.List;

@Component
@RequiredArgsConstructor
public class NotificationEventListener {
    private final NotificationPushService pushService;
    private final MemberCelebrityRepository memberCelebrityRepository;
    private final MemberRepository memberRepository;

    @Async("WhoReadsAsyncExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleFollowEvent(NotificationEvent.FollowEvent event) {

        FcmMessageDTO message =
                FcmMessageDTO.of(NotificationType.FOLLOW, event);

        List<MemberTokenDTO> memberTokens =
                memberCelebrityRepository
                        .findMemberTokensByCelebrityId(event.celebId());

        pushService.sendMulticast(
                memberTokens,
                message,
                event.idempotencyKey(),
                Duration.ofDays(3650)
        );
    }

    @Async("WhoReadsAsyncExecutor")
    @EventListener
    public void handleRoutineEvent(NotificationEvent.RoutineEvent event) {

        FcmMessageDTO message =
                FcmMessageDTO.of(NotificationType.ROUTINE, event);

        pushService.sendMulticast(
                event.memberTokens(),
                message,
                event.idempotencyKey(),
                Duration.ofDays(8)
        );
    }

    @Async("WhoReadsAsyncExecutor")
    @EventListener
    public void handleTimerCompletedEvent(
            NotificationEvent.TimerCompletedEvent event
    ) {
        FcmMessageDTO message =
                FcmMessageDTO.of(NotificationType.COMPLETED_TIMER, event);

        memberRepository.findById(event.memberId()).ifPresent(
                member -> pushService.sendMulticast(
                        List.of(new MemberToken(
                                member.getId(),
                                member.getFcmToken()
                        )),
                        message,
                        event.idempotencyKey(),
                        Duration.ofDays(30)
                )
        );
    }
}
