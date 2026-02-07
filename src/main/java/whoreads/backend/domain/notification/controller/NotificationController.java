package whoreads.backend.domain.notification.controller;

import jakarta.validation.Valid;
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
@RequestMapping("/api/notifications")
public class NotificationController implements NotificationControllerDocs {

    private final NotificationPushService notificationPushService;
    private final MemberRepository memberRepository;
    private final NotificationService notificationService;

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

    // 알림 조회
    @GetMapping()
    public ApiResponse<NotificationResDTO.TotalSettingDTO> getNotifications (
            @AuthenticationPrincipal Long memberId,
            @RequestParam(value = "type",required = false) String notificationType
    ){
        return ApiResponse.success(
               "사용자의 알림들을 성공적으로 조회하였습니다.",
               notificationService.getNotifications(memberId,notificationType)
       );
    };
    // 알림 생성
    @PostMapping()
    public ApiResponse<NotificationResDTO.SettingDTO> createNotifications(
            @AuthenticationPrincipal Long memberId,
            @RequestBody @Valid NotificationReqDTO.SettingDTO createSettingDTO
    ){
        return ApiResponse.success(
                "알림을 성공적으로 생성하였습니다.",
                notificationService.createNotification(memberId,createSettingDTO)
        );
    }
    // 알림 업데이트
    @PatchMapping("/me/{notification_id}")
    public ApiResponse<NotificationResDTO.SettingDTO> updateNotification(
            @AuthenticationPrincipal Long memberId,
            @PathVariable(value = "notification_id") Long notificationId,
            @RequestBody @Valid NotificationReqDTO.SettingDTO updateSettingDTO
    ){
        return ApiResponse.success(
                "알림을 성공적으로 수정하였습니다.",
                notificationService.updateNotification(memberId,notificationId,updateSettingDTO)
        );
    }
    // 알림 삭제
    @DeleteMapping("/me/{notification_id}")
    public ApiResponse<Void> deleteNotification(
            @AuthenticationPrincipal Long memberId,
            @PathVariable(value = "notification_id") Long notificationId
    ){
        notificationService.deleteNotification(memberId,notificationId);
        return ApiResponse.success("알림을 성공적으로 삭제하였습니다.");
    }
}
