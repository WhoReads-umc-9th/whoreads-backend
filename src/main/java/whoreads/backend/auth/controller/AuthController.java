package whoreads.backend.auth.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import whoreads.backend.auth.dto.AuthReqDto;
import whoreads.backend.auth.dto.AuthResDto;
import whoreads.backend.auth.service.AuthService;
import whoreads.backend.auth.service.EmailService;
import whoreads.backend.auth.service.KakaoOAuthClient;
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

    private final AuthService authService;
    private final EmailService emailService;
    private final KakaoOAuthClient kakaoOAuthClient;

    // TODO: 로그인 API - POST /api/auth/login
    // TODO: 회원가입 API - POST /api/auth/signup
    // TODO: 로그아웃 API - POST /api/auth/logout
    // TODO: Refresh Token 갱신 API - POST /api/auth/refresh

    @Override
    @PostMapping("/signup")
    public ApiResponse<AuthResDto.JoinData> signUp(@RequestBody @Valid AuthReqDto.SignUpRequest request) {

        AuthResDto.JoinData data = authService.signup(request);

        return ApiResponse.created("회원가입에 성공했습니다.", data);
    }

    @Override
    @PostMapping("/login")
    public ApiResponse<AuthResDto.TokenData> login(@RequestBody @Valid AuthReqDto.LoginRequest request) {

        AuthResDto.TokenData tokenData = authService.login(request);

        return ApiResponse.success("로그인에 성공했습니다.", tokenData);
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
    @PostMapping("/kakao/login/token")
    public ApiResponse<AuthResDto.KakaoLoginData> kakaoLoginWithToken(@RequestBody @Valid AuthReqDto.KakaoTokenLoginRequest request) {
        AuthResDto.KakaoLoginData data = authService.kakaoLoginWithToken(request);

        return ApiResponse.success("카카오 로그인 처리가 완료되었습니다.", data);
    }

    @Override
    @PostMapping("/kakao/signup")
    public ApiResponse<AuthResDto.TokenData> kakaoSignup(@RequestBody @Valid AuthReqDto.KakaoSignUpRequest request) {
        AuthResDto.TokenData tokenData = authService.kakaoSignup(request);

        return ApiResponse.created("카카오 회원가입에 성공했습니다.", tokenData);
    }

    // 앱 없이 브라우저만으로 카카오 로그인을 테스트하기 위한 인가 코드 콜백 (개발/테스트 용도)
    @GetMapping("/kakao/callback")
    public ApiResponse<AuthResDto.KakaoLoginData> kakaoCallback(@RequestParam String code) {
        String accessToken = kakaoOAuthClient.exchangeCodeForAccessToken(code);
        AuthResDto.KakaoLoginData data = authService.kakaoLoginWithToken(new AuthReqDto.KakaoTokenLoginRequest(accessToken));

        return ApiResponse.success("카카오 인가 코드로 로그인 처리가 완료되었습니다.", data);
    }

    @Override
    @PatchMapping("/delete")
    public ApiResponse<Void> delete(@AuthenticationPrincipal Long memberId) {
        authService.delete(memberId);

        return ApiResponse.success("회원 탈퇴 접수가 완료되었습니다. 7일 이내에 재로그인하시면 탈퇴를 취소할 수 있습니다.");
    }

    // 아이디 중복 확인
    @PostMapping("/check-id")
    public ApiResponse<Void> checkLoginId(@RequestBody @Valid AuthReqDto.CheckIdRequest request) {
        authService.checkLoginIdDuplicate(request.loginId());
        return ApiResponse.success("사용 가능한 아이디입니다.");
    }

    @PostMapping("/email/send")
    public ApiResponse<Void> sendEmail(@RequestBody @Valid AuthReqDto.EmailRequest request) {
        emailService.sendVerificationCode(request.email());

        return ApiResponse.success("인증번호가 발송되었습니다.");
    }

    @PostMapping("/email/verify")
    public ApiResponse<Void> verifyEmail(@RequestBody @Valid AuthReqDto.VerificationRequest request) {
        emailService.verifyCode(request.email(), request.code());

        return ApiResponse.success("이메일 인증에 성공했습니다.");
    }

    // 아이디 찾기
    @Override
    @PostMapping("/find-id")
    public ApiResponse<Void> findLoginId(@RequestBody @Valid AuthReqDto.EmailRequest request) {
        authService.findLoginId(request.email());

        return ApiResponse.success("가입하신 이메일로 아이디를 발송했습니다.");
    }

    // 비밀번호 재설정
    @PatchMapping("/password")
    public ApiResponse<Void> updatePassword(@AuthenticationPrincipal Long memberId, @RequestBody @Valid AuthReqDto.PasswordChangeRequest request) {
        authService.changePassword(memberId, request);

        return ApiResponse.success("비밀번호가 변경되었습니다.");
    }

}
