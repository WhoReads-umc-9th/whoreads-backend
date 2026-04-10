package whoreads.backend.domain.member.controller.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import whoreads.backend.domain.member.dto.MemberRequest;
import whoreads.backend.domain.member.dto.MemberResDto;
import whoreads.backend.global.response.ApiResponse;

import java.util.List;

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

    @Operation(
            summary = "사용자 개인 정보 조회"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "회원 없음"),
    })
    ApiResponse<MemberResDto.MemberInfo> getMyInfo(@AuthenticationPrincipal Long memberId);


    @Operation(
            summary = "사용자가 팔로우하는 유명인 리스트 조회"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "회원 없음"),
    })
    ApiResponse<List<MemberResDto.CelebrityFollow>> getMyFollows(@AuthenticationPrincipal Long memberId);


    @Operation(
            summary = "사용자가 유명인을 팔로우할 때 사용하는 API"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "팔로우 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "회원 없음 / 유명인 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 팔로우 중인 유명인"),
    })
    ApiResponse<Void> followCelebrity(@PathVariable Long celebrityId, @AuthenticationPrincipal Long memberId);
}