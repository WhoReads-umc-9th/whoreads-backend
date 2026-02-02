package whoreads.backend.domain.member.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
            // AUTH FILTER가 작동되면 userId를 제거하고 주석을 삭제해주세요!
//            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam Long userId,
            @RequestBody @Valid MemberRequest.FcmTokenRequest fcmRequest) {

        // notificationTokenService.updateToken(userPrincipal.getId(),fcmRequest.fcmToken());
        notificationTokenService.updateToken(userId,fcmRequest.fcmToken());
        return ApiResponse.success("토큰이 성공적으로 등록되었습니다.");
    }
    @DeleteMapping("me/fcm-tokens")
    @Override
    public ApiResponse<Void> deleteFcmToken(
            // AUTH FILTER가 작동되면 userId를 제거하고 주석을 삭제해주세요!
//            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam Long userId) {

        // notificationTokenService.deleteToken(userPrincipal.getId());
        notificationTokenService.deleteToken(userId);
        return ApiResponse.success("토큰이 성공적으로 삭제되었습니다.");
    }
}
