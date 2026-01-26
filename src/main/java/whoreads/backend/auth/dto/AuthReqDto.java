package whoreads.backend.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public class AuthReqDto {
    // 로그인 및 회원가입시 사용
    public record JoinRequest(
            @Schema(description = "이메일 아이디", example = "test")
            @NotBlank
            String emailId,

            @Schema(description = "이메일 도메인", example = "naver.com")
            @NotBlank
            String emailDomain,

            @Schema(description = "비밀번호", example = "password1234!")
            @NotBlank
            String password
    ){
        public String fullEmail() {
            return emailId + "@" + emailDomain;
        }
    }

    public record RefreshRequest(
        @Schema(description = "리프레시 토큰", example = "eyJhbGciOiJIUzI1NiJ...")
        @NotBlank
        String refreshToken
    ){}
}
