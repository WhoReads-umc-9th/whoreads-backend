package whoreads.backend.auth.dto;

import lombok.Builder;

import java.time.LocalDateTime;

public class AuthResDto {

    @Builder
    public record JoinData(
//            @Schema(description = "액세스 토큰", example = "eyJhbGciOiJIUzI1...")
            String accessToken,
            MemberInfo member
    ){}

    @Builder
    public record MemberInfo(
            Long id,
            String loginId,
            String email,
            LocalDateTime createdAt
    ){}

    @Builder
    public record TokenData(
            Long memberId,
            String grantType,
            String accessToken,
            String refreshToken,
            Long accessTokenExpiresIn
    ){}
}
