package whoreads.backend.domain.member.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import whoreads.backend.auth.principal.CustomUserDetails;
import whoreads.backend.domain.member.controller.docs.MemberControllerDocs;
import whoreads.backend.domain.member.dto.MemberRequest;
import whoreads.backend.domain.member.dto.MemberResDto;
import whoreads.backend.domain.member.service.MemberService;
import whoreads.backend.domain.notification.service.NotificationTokenService;
import whoreads.backend.global.response.ApiResponse;

import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members")
public class MemberController implements MemberControllerDocs {

    private final MemberService memberService;
    private final NotificationTokenService notificationTokenService;

    @GetMapping("/me")
    public ApiResponse<MemberResDto.MemberInfo> getMyInfo(@AuthenticationPrincipal CustomUserDetails userDetails) {
        // @AuthenticationPrincipal을 통해 JwtAuthenticationFilter에서 저장한 유저 정보를 바로 가져옵니다.
        return ApiResponse.success(memberService.getMemberInfo(userDetails.getMember().getId()));
    }

    // 내가 팔로우한 유명인 리스트 조회
    @GetMapping("/me/follows")
    public ApiResponse<List<MemberResDto.CelebrityFollow>> getMyFollows(@AuthenticationPrincipal Long memberId) {
        List<MemberResDto.CelebrityFollow> followList = memberService.getFollowList(memberId);

        return ApiResponse.success(followList);
    }

    @PostMapping ("me/fcm-tokens")
    @Override
    public ApiResponse<Void> updateFcmToken(
            @AuthenticationPrincipal Long memberId,
            @RequestBody @Valid MemberRequest.FcmTokenRequest fcmRequest) {

        notificationTokenService.updateToken(memberId,fcmRequest.fcmToken());
        return ApiResponse.success("토큰이 성공적으로 등록되었습니다.");
    }
    @DeleteMapping("me/fcm-tokens")
    @Override
    public ApiResponse<Void> deleteFcmToken(
            @AuthenticationPrincipal Long memberId) {

        notificationTokenService.deleteToken(memberId);
        return ApiResponse.success("토큰이 성공적으로 삭제되었습니다.");
    }
}
