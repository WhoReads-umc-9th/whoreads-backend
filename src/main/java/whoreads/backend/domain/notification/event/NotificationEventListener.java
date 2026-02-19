package whoreads.backend.domain.notification.event;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import whoreads.backend.domain.member.repository.MemberCelebrityRepository;
import whoreads.backend.domain.notification.dto.FcmMessageDTO;
import whoreads.backend.domain.notification.dto.MemberTokenDTO;
import whoreads.backend.domain.notification.enums.NotificationType;
import whoreads.backend.domain.notification.service.NotificationPushService;

import java.util.List;

@Component
@RequiredArgsConstructor
public class NotificationEventListener {
    private final NotificationPushService pushService;
    private final MemberCelebrityRepository memberCelebrityRepository;

    @Async("WhoReadsAsyncExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleFollowEvent(NotificationEvent.FollowEvent event) {
        FcmMessageDTO message = FcmMessageDTO.of(NotificationType.FOLLOW, event);
        List<MemberTokenDTO> memberTokens = memberCelebrityRepository
                .findMemberTokensByCelebrityId(event.celebId());
        // 알림 기록할 수 있게 넣어주기
        pushService.sendMulticast(memberTokens,message);
    }
    @Async("WhoReadsAsyncExecutor")
    @EventListener
    public void handleRoutineEvent(NotificationEvent.RoutineEvent event) {
        FcmMessageDTO message = FcmMessageDTO.of(NotificationType.ROUTINE,event);
        pushService.sendMulticast(event.memberTokens(),message);
    }
}
