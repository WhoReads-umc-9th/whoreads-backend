package whoreads.backend.auth.dto;

import lombok.Builder;

import java.time.LocalDateTime;

public class AuthResDto {

    public record JoinData(
//            @Schema(description = "액세스 토큰", example = "eyJhbGciOiJIUzI1...")
            String accessToken,
            MemberInfo member
    ){}

    @Builder
    public record MemberInfo(
//            @Schema(description = "사용자 고유ID", example = "1")
            Long id,
//            @Schema(description = "사용자 이메일ID", example = "test")
            String emailId,
            String emailDomain,
//            @Schema(description = "생성 일시", example = "2026-01-22T22:00:00")
            LocalDateTime createdAt
    ){}

    @Builder
    public record LoginData(
            Long memberId,
            String accessToken,
            String refreshToken
    ){}

    @Builder
    public record TokenData(
            String accessToken,
            String refreshToken
    ){}
}
