package whoreads.backend.domain.notification.event;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import whoreads.backend.domain.member.entity.Member;
import whoreads.backend.domain.member.repository.MemberRepository;
import whoreads.backend.domain.notification.dto.FcmMessageDTO;
import whoreads.backend.domain.notification.enums.NotificationType;
import whoreads.backend.domain.notification.service.NotificationPushService;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class NotificationEventListener {
    private final NotificationPushService pushService;
    private final MemberRepository memberRepository;

    @Async("WhoReadsAsyncExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleFollowEvent(NotificationEvent.FollowEvent event) {
        FcmMessageDTO message = FcmMessageDTO.of(NotificationType.FOLLOW, event);

        /* todo: 해당 유명인을 팔로우 한 사람들 찾기
            findById 사용시 N+1 문제 발생 -> 추후 유명인 팔로우 도메인 완성시 수정 예정!
         */
        List<Long> followList = List.of(2L,3L);
        List<String> tokens = followList.stream()
                .map(memberRepository::findById)
                .flatMap(Optional::stream)
                .map(Member::getFcmToken)
                .filter(token -> token != null && !token.isBlank())
                .toList();

        pushService.sendMulticast(tokens,message);
    }
    @Async("WhoReadsAsyncExecutor")
    @EventListener
    public void handleRoutineEvent(NotificationEvent.RoutineEvent event) {
        FcmMessageDTO message = FcmMessageDTO.of(NotificationType.ROUTINE,event);
        pushService.sendMulticast(event.tokens(),message);
    }
}
