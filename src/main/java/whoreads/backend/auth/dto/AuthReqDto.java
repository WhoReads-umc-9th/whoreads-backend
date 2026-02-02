package whoreads.backend.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import whoreads.backend.domain.member.enums.AgeGroup;
import whoreads.backend.domain.member.enums.Gender;

public class AuthReqDto {

    public record EmailRequest(
            @Schema(description = "이메일", example = "woody123@naver.com")
            @NotBlank
            String email
    ){}

    public record VerificationRequest(
            @Schema(description = "이메일", example = "woody123@naver.com")
            @NotBlank @Email
            String email,

            @Schema(description = "인증번호", example = "password1234!")
            @NotBlank String code
    ){}

    // JSON 최상위 {} 역할
    public record SignUpRequest(
            @Valid JoinRequest request,
            @Valid MemberInfo memberInfo
    ) {}

    // 회원가입시 사용
    public record JoinRequest(
            @Schema(description = "로그인 아이디", example = "woody123")
            @NotBlank
            String loginId,

            @Schema(description = "인증 완료된 이메일", example = "woody123@naver.com")
            @NotBlank
            String email,

            @Schema(description = "비밀번호", example = "password1234!")
            @NotBlank
            String password
    ){}

    public record LoginRequest(
            @Schema(description = "로그인 아이디", example = "woody123")
            @NotBlank String loginId,
            @Schema(description = "비밀번호", example = "password1234!")
            @NotBlank String password
    ){}

    public record RefreshRequest(
        @Schema(description = "리프레시 토큰", example = "eyJhbGciOiJIUzI1NiJ...")
        @NotBlank
        String refreshToken
    ){}

    public record MemberInfo(
            @NotBlank
            String nickname,
            Gender gender,
            AgeGroup ageGroup
    ){}
}
