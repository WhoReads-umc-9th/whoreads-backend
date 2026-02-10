package whoreads.backend.domain.member.controller.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;
import whoreads.backend.domain.member.dto.MemberRequest;
import whoreads.backend.global.response.ApiResponse;

@Tag(name = "Member (사용자)", description = "사용자 프로필 및 인증 관련 API")
public interface MemberControllerDocs {

    @Operation(
            summary = "FCM 토큰 업데이트",
            description = "사용자의 푸시 알림 FCM 토큰을 등록하거나 최신화합니다. <br><br>" +
                    "**💡 가이드:** <br>" +
                    "1. 서버에서 **매일 새벽 2시에 30일 이상 미접속한 토큰을 자동 삭제**하므로, 프론트에서는 **앱을 실행할 때마다** 최신 토큰을 서버에 전송해 주세요. <br>" +
                    "2. 토큰이 만료되었거나 삭제된 상태에서 앱을 켜면 재등록이 필요합니다."
    )
    @ApiResponses(
            {@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "토큰 업데이트 성공"),
            })
    ApiResponse<Void> updateFcmToken(
            @AuthenticationPrincipal Long memberId,
            @RequestBody MemberRequest.FcmTokenRequest request
    );

    @Operation(
            summary = "FCM 토큰 삭제",
            description = "사용자가 로그아웃하거나 계정을 삭제할 때 서버에 저장된 토큰을 즉시 무효화합니다."
    )
    @ApiResponses(
            {@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "토큰 삭제 성공"),
            })
    ApiResponse<Void> deleteFcmToken(@AuthenticationPrincipal Long memberId);
}