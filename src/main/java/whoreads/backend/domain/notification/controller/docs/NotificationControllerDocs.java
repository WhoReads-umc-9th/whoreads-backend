package whoreads.backend.domain.notification.controller.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import whoreads.backend.domain.notification.dto.NotificationReqDTO;
import whoreads.backend.domain.notification.dto.NotificationResDTO;

@Tag(name = "Notification (알림)", description = "알림 설정 및 발송 API")
public interface NotificationControllerDocs {

    @Operation(summary = "알림 발송 api (테스트용)", description = "사용자의 기기에 테스트 푸시 알림을 발송합니다.")
    whoreads.backend.global.response.ApiResponse<Void> sendTestMessage(
            @AuthenticationPrincipal Long memberId
    );

    @Operation(summary = "알림 조회 api", description = "사용자의 모든 알림 설정을 조회합니다. 처음 진입 시 FOLLOW 설정이 없으면 자동으로 생성됩니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    whoreads.backend.global.response.ApiResponse<NotificationResDTO.TotalSettingDTO> getNotifications(
            @AuthenticationPrincipal Long memberId,
            @Parameter(description = "알림 타입 (FOLLOW, ROUTINE). 미입력 시 전체 조회")
            @RequestParam(value = "type", required = false) String notificationType
    );

    @Operation(summary = "알림 생성 api", description = "알림 설정을 생성합니다. <br>" +
            "1. **FOLLOW**: `type` 필수. 데이터가 이미 있으면 기존 설정을 업데이트합니다.<br>" +
            "2. **ROUTINE**: `type`, `days`, `time` 필수." +
            "day : MONDAY, TUESDAY,WEDNESDAY,THURSDAY,FRIDAY,SATURDAY,SUNDAY 또는 1~7")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "생성/업데이트 성공"),
            @ApiResponse(responseCode = "400", description = "필수 값 누락 (ROUTINE 시 시간/요일 미입력)"),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    whoreads.backend.global.response.ApiResponse<NotificationResDTO.SettingDTO> createNotifications(
            @AuthenticationPrincipal Long memberId,
            @RequestBody @Valid NotificationReqDTO.SettingDTO createSettingDTO
    );

    @Operation(summary = "알림 수정 api", description = "기존 알림 설정을 수정합니다. 본인의 알림만 수정 가능하며, 타입(FOLLOW/ROUTINE) 변경은 불가합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "403", description = "수정 권한 없음 (본인의 알림이 아님)"),
            @ApiResponse(responseCode = "404", description = "알림 존재하지 않음"),
            @ApiResponse(responseCode = "405", description = "타입 변경 시도 또는 잘못된 형식")
    })
    whoreads.backend.global.response.ApiResponse<NotificationResDTO.SettingDTO> updateNotification(
            @AuthenticationPrincipal Long memberId,
            @Parameter(description = "수정할 알림의 ID") @PathVariable Long notificationId,
            @RequestBody @Valid NotificationReqDTO.SettingDTO updateSettingDTO
    );

    @Operation(summary = "알림 삭제 api", description = "설정된 알림을 삭제합니다. 본인의 알림만 삭제 가능합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "삭제 성공"),
            @ApiResponse(responseCode = "403", description = "삭제 권한 없음 (본인의 알림이 아님)"),
            @ApiResponse(responseCode = "404", description = "알림 존재하지 않음")
    })
    whoreads.backend.global.response.ApiResponse<Void> deleteNotification(
            @AuthenticationPrincipal Long memberId,
            @Parameter(description = "삭제할 알림의 ID") @PathVariable Long notificationId
    );
}