package whoreads.backend.domain.notification.controller.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import whoreads.backend.domain.notification.dto.NotificationResDTO;

@Tag(name = "Notification (알림)", description = "알림 내역 조회 및 발송 테스트 API")
public interface NotificationControllerDocs {

    @Operation(summary = "테스트 알림 발송", description = "로그인한 사용자 본인에게 테스트 푸시 알림을 발송합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "알림 전송 성공"),
            @ApiResponse(responseCode = "404", description = "FCM 토큰 미등록 사용자", content = @Content)
    })
    whoreads.backend.global.response.ApiResponse<Void> sendTestMessage(
            @AuthenticationPrincipal Long memberId
    );

    @Operation(summary = "미수신 알림 내역 조회 (Inbox)", description = "사용자에게 도착한 알림 중 아직 읽지 않은(삭제되지 않은) 내역을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    whoreads.backend.global.response.ApiResponse<NotificationResDTO.TotalInboxDTO> getNotifications(
            @AuthenticationPrincipal Long memberId,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "10") Integer size
    );

    @Operation(summary = "알림 읽음 처리 (내역 삭제)", description = "사용자가 알림을 확인하면 해당 알림을 내역에서 즉시 삭제합니다.")
    @Parameter(name = "notification_id", description = "삭제할 알림의 고유 ID", example = "1")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "삭제 성공"),
            @ApiResponse(responseCode = "403",description = "자신의 알림이 아님"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 알림 ID", content = @Content)
    })
    whoreads.backend.global.response.ApiResponse<Void> deleteNotification(
            @AuthenticationPrincipal Long memberId,
            @PathVariable(value = "notification_id") Long notificationId
    );
}