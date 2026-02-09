package whoreads.backend.domain.notification.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;
import whoreads.backend.domain.notification.controller.docs.NotificationControllerDocs;
import whoreads.backend.domain.notification.service.NotificationPushService;
import whoreads.backend.global.response.ApiResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController implements NotificationControllerDocs {

    private final NotificationPushService notificationPushService;

    @Profile("!prod")
    @PostMapping("/test")
    public ApiResponse<Void> sendTestMessage(
            @RequestParam String title,
            @RequestParam(required = false) String body,
            @RequestParam String fcmToken,
            @RequestParam(required = false) String link
    ) {
        notificationPushService.sendNotification(title,body,fcmToken,link);
        return ApiResponse.success("알림이 전송되었습니다.");
    }
}
