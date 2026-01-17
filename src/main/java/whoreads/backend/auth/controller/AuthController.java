package whoreads.backend.auth.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import whoreads.backend.auth.service.AuthService;

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
public class AuthController {

    private final AuthService authService;

    // TODO: 로그인 API - POST /api/auth/login
    // TODO: 회원가입 API - POST /api/auth/signup
    // TODO: 로그아웃 API - POST /api/auth/logout
    // TODO: Refresh Token 갱신 API - POST /api/auth/refresh
}
