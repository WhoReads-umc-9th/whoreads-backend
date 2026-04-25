package whoreads.backend.domain.quote.dto;

import tools.jackson.annotation.JsonProperty;
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

    @JsonProperty("book_id")
    @NotNull(message = "책 ID는 필수입니다.")
    @Positive(message = "책 ID는 양수여야 합니다.")
    private Long bookId;

    @JsonProperty("celebrity_id")
    @NotNull(message = "유명인 ID는 필수입니다.")
    @Positive(message = "유명인 ID는 양수여야 합니다.")
    private Long celebrityId;

    @JsonProperty("original_text")
    @NotBlank(message = "인용 문구는 필수입니다.")
    @Size(max = 2000, message = "인용 문구는 2000자를 초과할 수 없습니다.")
    private String originalText;

    @NotNull(message = "언어 설정은 필수입니다.")
    private Quote.Language language;

    @JsonProperty("context_score")
    @NotNull(message = "맥락 점수는 필수입니다.")
    @Min(value = 0, message = "맥락 점수는 0 이상이어야 합니다.")
    private Integer contextScore;

    @Valid
    private SourceInfo source;

    @Valid
    private ContextInfo context;

    @Getter
    @NoArgsConstructor
    public static class SourceInfo {
        @URL(message = "올바른 URL 형식이어야 합니다.")
        private String url;
        private QuoteSourceType type;
        private String timestamp;
        @JsonProperty("is_direct") private boolean isDirect;
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
