package whoreads.backend.auth.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import whoreads.backend.auth.dto.AuthReqDto;
import whoreads.backend.auth.dto.AuthResDto;
import whoreads.backend.auth.service.AuthServiceImpl;
import whoreads.backend.auth.service.EmailService;
import whoreads.backend.global.response.ApiResponse;

/**
 * 인증 관련 API Controller
 *
 * [TODO] Refresh Token 갱신 API 구현 필요
 * - POST /api/auth/refresh
 *
 * [이유]
 * - Access Token 유효기간: 1시간
 * - Refresh Token 유효기간: 7일
 * - Access Token 만료 시, Refresh Token으로 새 Access Token을 발급받는 API가 없으면
 *   사용자는 1시간마다 재로그인해야 함
 * - Refresh Token 갱신 API가 있으면 7일간 자동 로그인 유지 가능
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController implements AuthControllerDocs {

//    private final AuthService authService;
    private final AuthServiceImpl authService;
    private final EmailService emailService;

    // TODO: 로그인 API - POST /api/auth/login
    // TODO: 회원가입 API - POST /api/auth/signup
    // TODO: 로그아웃 API - POST /api/auth/logout
    // TODO: Refresh Token 갱신 API - POST /api/auth/refresh

    @Override
    @PostMapping("/signup")
    public ApiResponse<AuthResDto.JoinData> signUp(@RequestBody AuthReqDto.SignUpRequest request) {

        AuthResDto.JoinData dummyData = authService.signup(request);

        return ApiResponse.success(dummyData);
    }

    @Override
    @PostMapping("/login")
    public ApiResponse<AuthResDto.TokenData> login(@RequestBody @Valid AuthReqDto.LoginRequest request) {

        AuthResDto.TokenData dummyData = authService.login(request);

        return ApiResponse.success(dummyData);
    }

    @Override
    @PostMapping("/logout")
    public ApiResponse<Void> logout(@AuthenticationPrincipal Long memberId) {
        // 토큰을 통해 검증된 memberId가 파라미터로 들어온다.
        authService.logout(memberId);

        return ApiResponse.success("로그아웃 완료");
    }

    @Override
    @PostMapping("/refresh")
    public ApiResponse<AuthResDto.TokenData> refresh(@RequestBody @Valid AuthReqDto.RefreshRequest request) {
        AuthResDto.TokenData refresh = authService.refresh(request);

        return ApiResponse.success("엑세스 토큰 재발급이 완료되었습니다.", refresh);
    }

    @Override
    @PatchMapping("/delete")
    public ApiResponse<Void> delete(@AuthenticationPrincipal Long memberId) {
        authService.delete(memberId);

        return ApiResponse.success("회원 탈퇴 접수가 완료되었습니다. 7일 이내에 재로그인하시면 탈퇴를 취소할 수 있습니다.");
    }

    @PostMapping("/email/send")
    public ApiResponse<Void> sendEmail(@RequestBody @Valid AuthReqDto.EmailRequest request) {
        emailService.sendVerificationCode(request.email());

        return ApiResponse.success("인증번호가 발송되었습니다.");
    }

    @PostMapping("/email/verify")
    public ApiResponse<Void> verifyEmail(@RequestBody @Valid AuthReqDto.VerificationRequest request) {
        boolean isSuccess = emailService.verifyCode(request.email(), request.code());
        if (isSuccess)
            return ApiResponse.success("이메일 인증에 성공했습니다.");
        else
            return ApiResponse.error(400, "인증 번호가 올바르지 않습니다. 인증번호를 다시 입력해주세요.");
    }
}
