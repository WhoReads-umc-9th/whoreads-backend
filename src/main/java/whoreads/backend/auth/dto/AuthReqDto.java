package whoreads.backend.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import whoreads.backend.domain.member.enums.AgeGroup;
import whoreads.backend.domain.member.enums.Gender;

public class AuthReqDto {

    public record EmailRequest(
            @Schema(description = "이메일", example = "woody123@naver.com")
            @NotBlank @Email
            String email
    ){}

    public record VerificationRequest(
            @Schema(description = "이메일", example = "woody123@naver.com")
            @NotBlank @Email
            String email,

            @Schema(description = "인증번호", example = "123456")
            @NotBlank String code
    ){}

    // 아이디 중복 확인 요청
    public record CheckIdRequest(
            @Schema(description = "중복 확인할 아이디", example = "woody123")
            @NotBlank String loginId
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
            @NotBlank @Email
            String email,

            @Schema(description = "비밀번호", example = "password1234!")
            @NotBlank
            @Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다.")
            @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=\\S+$).+$",
                    message = "비밀번호는 영문, 숫자를 포함해야 하며 공백을 사용할 수 없습니다.")
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
            @NotNull
            Gender gender,
            @NotNull
            AgeGroup ageGroup
    ){}
}
