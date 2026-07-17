package whoreads.backend.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.time.LocalDateTime;

public class AuthResDto {

    @Builder
    public record JoinData(
            @JsonProperty("access_token") String accessToken,
            MemberInfo member
    ){}

    @Builder
    public record MemberInfo(
            Long id,
            @JsonProperty("login_id") String loginId,
            String email,
            @JsonProperty("created_at") LocalDateTime createdAt
    ){}

    @Builder
    public record TokenData(
            @JsonProperty("member_id") Long memberId,
            @JsonProperty("grant_type") String grantType,
            @JsonProperty("access_token") String accessToken,
            @JsonProperty("refresh_token") String refreshToken,
            @JsonProperty("access_token_expires_in") Long accessTokenExpiresIn
    ){}

    @Builder
    public record KakaoLoginData(
            // true: 신규 회원 -> registrationToken으로 /kakao/signup 호출 필요
            // false: 기존 회원 -> tokenData로 바로 로그인 완료
            @JsonProperty("is_new_member")
            boolean isNewMember,
            @JsonProperty("token_data")
            TokenData tokenData,
            @JsonProperty("registration_token")
            String registrationToken,
            String email,
            String nickname
    ){}
}
