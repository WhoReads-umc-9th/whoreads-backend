package whoreads.backend.auth.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;
import whoreads.backend.auth.dto.AuthResDto;

import java.security.Key;
import java.util.Collections;
import java.util.Date;

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
@Slf4j
@Component
public class JwtTokenProvider {

    private final Key key;
    private final long accessTokenValidityTime;
    private final long refreshTokenValidityTime;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secretKey,
            @Value("${jwt.access-token-validity}") long accessTokenValidityTime,
            @Value("${jwt.refresh-token-validity}") long refreshTokenValidityTime) {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.accessTokenValidityTime = accessTokenValidityTime;
        this.refreshTokenValidityTime = refreshTokenValidityTime;
    }

    public String createAccessToken(Long memberId) {
        long now = new Date().getTime();

        return Jwts.builder()
                .setSubject(String.valueOf(memberId)) // 토큰 안에 사용자ID 저장
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + accessTokenValidityTime))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String createRefreshToken(Long memberId) {
        long now = new Date().getTime();

        return Jwts.builder()
                .setSubject(String.valueOf(memberId))
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + refreshTokenValidityTime))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // 토큰 유효성 체크
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            log.info("JWT 검증 실패: {}", e.getMessage());
        }
        return false;
    }

    public Long getMemberIdFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return Long.parseLong(claims.getSubject());
    }

    public AuthResDto.TokenData generateTokenResponse(Long memberId) {
        String accessToken = createAccessToken(memberId);
        String refreshToken = createRefreshToken(memberId);

        return AuthResDto.TokenData.builder()
                .memberId(memberId)
                .grantType("Bearer")
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .accessTokenExpiresIn(accessTokenValidityTime)
                .build();
    }
}
