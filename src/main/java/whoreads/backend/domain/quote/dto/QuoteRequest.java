package whoreads.backend.domain.quote.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.hibernate.validator.constraints.URL;
import lombok.Getter;
import lombok.NoArgsConstructor;
import whoreads.backend.domain.quote.entity.Quote;
import whoreads.backend.domain.quote.entity.QuoteSourceType;

@Getter
@NoArgsConstructor
public class QuoteRequest {

    // 1. 기본 정보
    @NotNull(message = "책 ID는 필수입니다.")
    @Positive(message = "책 ID는 양수여야 합니다.") // 바꾼 이유: ID 값에 음수가 들어오는 것을 방지
    private Long bookId;

    @NotNull(message = "유명인 ID는 필수입니다.")
    @Positive(message = "유명인 ID는 양수여야 합니다.") // 바꾼 이유: ID 값에 음수가 들어오는 것을 방지
    private Long celebrityId;

    @NotBlank(message = "인용 문구는 필수입니다.")
    private String originalText;

    private Quote.Language language; // 없을 경우 로직에서 기본값 처리 가능

    @Min(value = 0, message = "맥락 점수는 0 이상이어야 합니다.") // 바꾼 이유: 점수가 음수로 들어오는 논리적 오류 차단
    private int contextScore;

    // 2. 출처 정보 (선택)
    @Valid // 바꾼 이유: 중첩된 객체(SourceInfo) 내부의 필드 검증(@URL 등)을 활성화하기 위해 추가
    private SourceInfo source;

    // 3. 맥락 정보 (선택)
    private ContextInfo context;

    @Getter
    @NoArgsConstructor
    public static class SourceInfo {
        @URL(message = "올바른 URL 형식이어야 합니다.") // 바꾼 이유: 잘못된 형식의 URL이 DB에 저장되는 것 방지
        private String url;
        private QuoteSourceType type;
        private String timestamp;
        private boolean isDirect; // 직접 인용 여부 (true: 직접, false: 간접/추천사 아님 등)
    }

    @Getter
    @NoArgsConstructor
    public static class ContextInfo {
        private String how;  // 계기
        private String when; // 시기
        private String why;  // 이유
        private String help; // 도움
    }
}