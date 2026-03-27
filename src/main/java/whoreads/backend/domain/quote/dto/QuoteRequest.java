package whoreads.backend.domain.quote.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
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
    @Positive(message = "책 ID는 양수여야 합니다.")
    private Long bookId;

    @NotNull(message = "유명인 ID는 필수입니다.")
    @Positive(message = "유명인 ID는 양수여야 합니다.")
    private Long celebrityId;

    @NotBlank(message = "인용 문구는 필수입니다.")
    @Size(max = 2000, message = "인용 문구는 2000자를 초과할 수 없습니다.")
    private String originalText;

    private Quote.Language language;

    @Min(value = 0, message = "맥락 점수는 0 이상이어야 합니다.")
    private int contextScore;

    // 2. 출처 정보 (선택)
    @Valid // 바꾼 이유: 중첩된 객체(SourceInfo) 내부의 필드 검증(@URL 등)을 활성화하기 위해 추가
    private SourceInfo source;

    // 3. 맥락 정보 (선택)
    @Valid // 누락되었던 검증 활성화
    private ContextInfo context;

    @Getter
    @NoArgsConstructor
    public static class SourceInfo {
        @URL(message = "올바른 URL 형식이어야 합니다.")
        private String url;
        private QuoteSourceType type;
        private String timestamp;
        private boolean isDirect; // 직접 인용 여부 (true: 직접, false: 간접/추천사 아님 등)
    }

    @Getter
    @NoArgsConstructor
    public static class ContextInfo {
        @Size(max = 1000, message = "계기 설명은 1000자 이내여야 합니다.")
        private String how;

        @Size(max = 500, message = "시기 설명은 500자 이내여야 합니다.")
        private String when;

        @Size(max = 1000, message = "이유 설명은 1000자 이내여야 합니다.")
        private String why;

        @Size(max = 1000, message = "도움 설명은 1000자 이내여야 합니다.")
        private String help;
    }
}