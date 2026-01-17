package whoreads.backend.auth.jwt;

import org.springframework.stereotype.Component;

/**
 * JWT 토큰 생성 및 검증 Provider
 *
 * [토큰 유효기간] (application.yml 참고)
 * - Access Token: 1시간 (3600000ms)
 * - Refresh Token: 7일 (604800000ms)
 *
 * [TODO] 구현 필요 메서드
 * - createAccessToken(Long memberId): Access Token 생성
 * - createRefreshToken(Long memberId): Refresh Token 생성
 * - validateToken(String token): 토큰 유효성 검증
 * - getMemberIdFromToken(String token): 토큰에서 memberId 추출
 *
 * [Refresh Token 갱신 흐름]
 * 1. 클라이언트: Access Token 만료 감지 (401 응답)
 * 2. 클라이언트: Refresh Token으로 /api/auth/refresh 호출
 * 3. 서버: Refresh Token 검증 후 새 Access Token 발급
 * 4. 클라이언트: 새 Access Token으로 API 재요청
 */
@Component
public class JwtTokenProvider {

}
