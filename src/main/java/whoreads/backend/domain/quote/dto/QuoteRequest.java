package whoreads.backend.domain.quote.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import whoreads.backend.domain.quote.entity.Quote;
import whoreads.backend.domain.quote.entity.QuoteSourceType;

@Getter
@NoArgsConstructor
public class QuoteRequest {

    // 1. 기본 정보
    private Long bookId;
    private Long celebrityId;
    private String originalText;
    private Quote.Language language;
    private int contextScore;

    // 2. 출처 정보
    private SourceInfo source;

    // 3. 맥락 정보
    private ContextInfo context;

    @Getter
    @NoArgsConstructor
    public static class SourceInfo {
        private String url;
        private QuoteSourceType type;
        private String timestamp;
        private boolean isDirect;
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