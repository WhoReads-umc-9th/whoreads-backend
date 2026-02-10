package whoreads.backend.domain.notification.event;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import whoreads.backend.domain.member.repository.MemberRepository;
import whoreads.backend.domain.notification.dto.FcmMessageDTO;
import whoreads.backend.domain.notification.dto.MemberTokenDTO;
import whoreads.backend.domain.notification.enums.NotificationType;
import whoreads.backend.domain.notification.service.NotificationPushService;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
        
        // todo: 멤버 아이디가 들어있는 리스트로 가져오기 , 이것도 추후 구현
        List<MemberTokenDTO> memberTokens = followList.stream()
                .map(memberRepository::findById)
                .flatMap(Optional::stream)
                .filter(member -> member.getFcmToken() != null && !member.getFcmToken().isBlank())
                .map(member -> new MemberTokenDTO() {  // 익명 객체로 인터페이스 구현
                    @Override
                    public Long getMemberId() { return member.getId(); }
                    @Override
                    public String getFcmToken() { return member.getFcmToken(); }
                })
                .collect(Collectors.toList());
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
