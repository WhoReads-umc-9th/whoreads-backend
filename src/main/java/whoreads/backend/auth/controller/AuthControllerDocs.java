package whoreads.backend.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import whoreads.backend.auth.dto.AuthReqDto;
import whoreads.backend.auth.dto.AuthResDto;
import whoreads.backend.global.response.ApiResponse;

public interface AuthControllerDocs {

    @Operation(summary = "회원가입 기능 개발 By 우디 (개발중)")
    @ApiResponses(
            {@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
//            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "실패")
    })
    ApiResponse<AuthResDto.JoinData> signUp(@RequestBody AuthReqDto.SignUpRequest request);


    @Operation(summary = "로그인 기능 개발 By 우디 (개발중)")
    @ApiResponses({@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공")})
    ApiResponse<AuthResDto.TokenData> login(@RequestBody @Valid AuthReqDto.LoginRequest dto);


    @Operation(summary = "로그아웃 기능 개발 By 우디 (개발중)",
            description = "헤더의 Access Token을 읽어 현재 로그인한 사용자를 로그아웃 처리합니다",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "로그아웃 성공"),
//            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "유효하지 않거나 만료된 토큰입니다.") // 새롭게 추가될 에러
    })
    ApiResponse<Void> logout(@AuthenticationPrincipal Long memberId);


    @Operation(summary = "토큰 재발급 By 우디 (개발중)")
    @ApiResponses
    ApiResponse<AuthResDto.TokenData> refresh(@RequestBody @Valid AuthReqDto.RefreshRequest request);


    @Operation(
            summary = "회원 탈퇴 API",
            description = "현재 로그인한 사용자의 계정을 즉시 탈퇴하지 않습니다. 비활성화 상태로 변경하고 7일이 지나면 토큰을 삭제하고 계정을 탈퇴합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "탈퇴 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    ApiResponse<Void> delete(@AuthenticationPrincipal Long memberId);


    @Operation(summary = "이메일 인증번호 발송",
            description = "회원가입을 위해 입력한 이메일로 6자리 인증 코드를 발송합니다. \n\n" +
                    "- 발송된 코드는 **Redis에 5분간 저장**됩니다. \n\n" +
                    "- 네이버/구글 SMTP 서버를 통해 실제 메일이 발송됩니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "인증번호 발송 성공"),
//            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "메일 서버 통신 실패")
    })
    ApiResponse<Void> sendEmail(@RequestBody @Valid AuthReqDto.EmailRequest request);


    @Operation(summary = "이메일 인증번호 검증",
            description = "사용자가 입력한 인증번호가 유효한지 확인합니다. \n\n" +
                    "Redis에 저장된 코드와 일치하는지 대조합니다. \n\n")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "이메일 인증 성공"),
//            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "인증번호 불일치 혹은 만료")
    })
            ApiResponse<Void> verifyEmail(@RequestBody @Valid AuthReqDto.VerificationRequest request);
}
