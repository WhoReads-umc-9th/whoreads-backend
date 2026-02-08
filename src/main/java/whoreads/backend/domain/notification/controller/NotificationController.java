package whoreads.backend.domain.notification.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import whoreads.backend.domain.member.entity.Member;
import whoreads.backend.domain.member.repository.MemberRepository;
import whoreads.backend.domain.notification.controller.docs.NotificationControllerDocs;
import whoreads.backend.domain.notification.dto.*;
import whoreads.backend.domain.notification.enums.NotificationType;
import whoreads.backend.domain.notification.service.*;
import whoreads.backend.global.exception.*;
import whoreads.backend.global.response.ApiResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications/me")
public class NotificationController implements NotificationControllerDocs {

    private final NotificationPushService notificationPushService;
    private final MemberRepository memberRepository;
    private final NotificationHistoryService notificationHistoryService;

    @PostMapping("/test")
    public ApiResponse<Void> sendTestMessage(
            @AuthenticationPrincipal Long memberId
    ) {
        Member member = memberRepository.findById(memberId).orElseThrow();
        String fcmToken = member.getFcmToken();
        if (fcmToken==null || fcmToken.isEmpty())
            throw new CustomException(ErrorCode.FCM_TOKEN_UNREGISTERED);
        notificationPushService.sendNotification
                (fcmToken,FcmMessageDTO.of(NotificationType.ROUTINE,null));
        return ApiResponse.success("알림이 전송되었습니다.");
    }
    // 미수신 알림 조회
    @GetMapping("/inbox")
    public ApiResponse<NotificationResDTO.TotalInboxDTO> getNotifications (
            @AuthenticationPrincipal Long memberId,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "10") Integer size
    ){
        return ApiResponse.success(
               "사용자의 알림들을 성공적으로 조회하였습니다.",
                notificationHistoryService.getNotificationHistory(memberId,cursor,size)
       );
    }
    // 알림 수신 처리 ( 삭제 )
    @DeleteMapping("/inbox/{notification_id}")
    public ApiResponse<Void> deleteNotification(
            @AuthenticationPrincipal Long memberId,
            @PathVariable(value = "notification_id") Long notificationId
    ){
        notificationHistoryService.readNotification(memberId,notificationId);
        return ApiResponse.success("알림을 성공적으로 삭제하였습니다.");
    }
}
