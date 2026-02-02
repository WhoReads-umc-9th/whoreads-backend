package whoreads.backend.auth.service;

import whoreads.backend.auth.dto.AuthReqDto;
import whoreads.backend.auth.dto.AuthResDto;

/**
 * 인증 관련 비즈니스 로직 Service
 *
 * [TODO] Refresh Token 갱신 로직 구현 필요
 *
 * [구현 내용]
 * 1. 클라이언트가 Refresh Token을 전달
 * 2. Refresh Token 유효성 검증
 * 3. 유효하면 새로운 Access Token 발급 (+ 선택적으로 Refresh Token도 갱신)
 * 4. 유효하지 않으면 재로그인 요청 (401 Unauthorized)
 *
 * [이유]
 * - Access Token(1시간) 만료 시 Refresh Token(7일)으로 갱신하지 않으면
 *   사용자가 1시간마다 재로그인해야 하는 불편함 발생
 */
public interface AuthService {

    // TODO: 로그인 - login(LoginRequest request)
    // TODO: 회원가입 - signup(SignupRequest request)
    // TODO: 로그아웃 - logout(String accessToken)
    // TODO: 토큰 갱신 - refresh(String refreshToken) -> 새 Access Token 반환
    AuthResDto.JoinData signup(AuthReqDto.SignUpRequest request);

    AuthResDto.TokenData login(AuthReqDto.LoginRequest dto);

    void logout(Long memberId);

    AuthResDto.TokenData refresh(AuthReqDto.RefreshRequest dto);

    void delete(Long memberId);
}
