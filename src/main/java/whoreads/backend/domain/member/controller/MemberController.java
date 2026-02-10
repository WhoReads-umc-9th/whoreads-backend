package whoreads.backend.domain.member.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import whoreads.backend.domain.member.controller.docs.MemberControllerDocs;
import whoreads.backend.domain.member.dto.MemberRequest;
import whoreads.backend.domain.member.service.MemberService;
import whoreads.backend.domain.notification.service.NotificationTokenService;
import whoreads.backend.global.response.ApiResponse;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members")
public class MemberController implements MemberControllerDocs {

    private final MemberService memberService;
    private final NotificationTokenService notificationTokenService;

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
