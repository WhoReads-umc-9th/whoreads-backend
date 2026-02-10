package whoreads.backend.domain.quote.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import whoreads.backend.domain.quote.entity.Quote;
import whoreads.backend.domain.quote.entity.QuoteSourceType;

@Getter
@NoArgsConstructor
public class QuoteRequest {

    // 1. 기본 정보
    @NotNull(message = "책 ID는 필수입니다.")
    private Long bookId;

    @NotNull(message = "유명인 ID는 필수입니다.")
    private Long celebrityId;

    @NotNull(message = "인용 문구는 필수입니다.")
    private String originalText;

    private Quote.Language language; // 없을 경우 로직에서 기본값 처리 가능

    private int contextScore;

    // 2. 출처 정보 (선택)
    private SourceInfo source;

    // 3. 맥락 정보 (선택)
    private ContextInfo context;

    @Getter
    @NoArgsConstructor
    public static class SourceInfo {
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