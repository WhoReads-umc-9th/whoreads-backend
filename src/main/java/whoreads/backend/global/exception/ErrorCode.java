package whoreads.backend.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Common
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "잘못된 입력값입니다."),
    INVALID_TYPE_VALUE(HttpStatus.BAD_REQUEST, "잘못된 타입입니다."),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "리소스를 찾을 수 없습니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "허용되지 않은 메서드입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."),

    // Auth
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "만료된 토큰입니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),

    // Member
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "회원을 찾을 수 없습니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다."),
    INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "비밀번호가 올바르지 않습니다."),

    // Book
    BOOK_NOT_FOUND(HttpStatus.NOT_FOUND, "책을 찾을 수 없습니다."),

    // Celebrity
    CELEBRITY_NOT_FOUND(HttpStatus.NOT_FOUND, "유명인을 찾을 수 없습니다."),

    // Quote
    QUOTE_NOT_FOUND(HttpStatus.NOT_FOUND, "인용을 찾을 수 없습니다."),

    // DNA
    DNA_TEST_NOT_COMPLETED(HttpStatus.BAD_REQUEST, "독서 DNA 테스트를 완료해주세요."),

    // Reading Session
    SESSION_NOT_FOUND(HttpStatus.NOT_FOUND, "독서 세션을 찾을 수 없습니다."),
    SESSION_ALREADY_ACTIVE(HttpStatus.CONFLICT, "이미 진행 중인 독서 세션이 있습니다."),

    // Focus Mode
    FOCUS_MODE_NOT_FOUND(HttpStatus.NOT_FOUND, "집중 모드 설정을 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String message;
}
