package whoreads.backend.domain.notification.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import whoreads.backend.domain.notification.controller.docs.NotificationSettingControllerDocs;
import whoreads.backend.domain.notification.dto.NotificationReqDTO;
import whoreads.backend.domain.notification.dto.NotificationResDTO;
import whoreads.backend.domain.notification.service.NotificationSettingService;
import whoreads.backend.global.response.ApiResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications/me/settings")
public class NotificationSettingController implements NotificationSettingControllerDocs {

    private final NotificationSettingService notificationSettingService;

    // 알림 조회
    @GetMapping()
    public ApiResponse<NotificationResDTO.TotalSettingDTO> getNotificationSettings(
            @AuthenticationPrincipal Long memberId,
            @RequestParam(value = "type",required = false) String notificationType
    ){
        return ApiResponse.success(
               "사용자의 알림 설정들을 성공적으로 조회하였습니다.",
               notificationSettingService.getNotificationSettings(memberId,notificationType)
       );
    };
    // 알림 설정 생성
    @PostMapping()
    public ApiResponse<NotificationResDTO.SettingDTO> createNotificationSetting(
            @AuthenticationPrincipal Long memberId,
            @RequestBody @Valid NotificationReqDTO.SettingDTO createSettingDTO
    ){
        return ApiResponse.success(
                "알림 설정을 성공적으로 생성하였습니다.",
                notificationSettingService.createNotificationSetting(memberId,createSettingDTO)
        );
    }
    // 알림 설정 업데이트
    @PatchMapping("/{notification_setting_id}")
    public ApiResponse<NotificationResDTO.SettingDTO> updateNotificationSetting(
            @AuthenticationPrincipal Long memberId,
            @PathVariable(value = "notification_setting_id") Long notificationId,
            @RequestBody @Valid NotificationReqDTO.SettingDTO updateSettingDTO
    ){
        return ApiResponse.success(
                "알림 설정을 성공적으로 수정하였습니다.",
                notificationSettingService.updateNotificationSetting(memberId,notificationId,updateSettingDTO)
        );
    }
    // 알림 삭제
    @DeleteMapping("/{notification_setting_id}")
    public ApiResponse<Void> deleteNotificationSetting(
            @AuthenticationPrincipal Long memberId,
            @PathVariable(value = "notification_setting_id") Long notificationId
    ){
        notificationSettingService.deleteNotificationSetting(memberId,notificationId);
        return ApiResponse.success("알림 설정을 성공적으로 삭제하였습니다.");
    }
}
