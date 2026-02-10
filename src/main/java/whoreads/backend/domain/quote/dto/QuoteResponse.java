package whoreads.backend.domain.quote.dto;

import lombok.Builder;
import lombok.Getter;
import whoreads.backend.domain.book.entity.Book;
import whoreads.backend.domain.celebrity.entity.Celebrity;
import whoreads.backend.domain.quote.entity.Quote;
import whoreads.backend.domain.quote.entity.QuoteContext;
import whoreads.backend.domain.quote.entity.QuoteSource;

@Getter
@Builder
public class QuoteResponse {

    private Long id;
    private String originalText;
    private int contextScore;

    // 책 정보
    private Long bookId;
    private String bookTitle;
    private String bookCover;

    // 인물 정보
    private Long celebrityId;
    private String celebrityName;
    private String celebrityImg;
    private String celebrityJob;

    // 맥락 (Why, How)
    private ContextInfo context;

    // 출처 (Link)
    private SourceInfo source;

    @Getter @Builder
    public static class ContextInfo {
        private String how;
        private String when;
        private String why;
        private String help;
    }

    @Getter @Builder
    public static class SourceInfo {
        private String url;
        private String type; // Enum Name or Description
        private String timestamp;
    }

    public static QuoteResponse of(Quote quote, Book book, Celebrity celebrity, QuoteContext ctx, QuoteSource src) {
        return QuoteResponse.builder()
                .id(quote.getId())
                .originalText(quote.getOriginalText())
                .contextScore(quote.getContextScore())
                // 책 정보
                .bookId(book.getId())
                .bookTitle(book.getTitle())
                .bookCover(book.getCoverUrl())
                // 인물 정보
                .celebrityId(celebrity.getId())
                .celebrityName(celebrity.getName())
                .celebrityImg(celebrity.getImageUrl())
                .celebrityJob(celebrity.getShortBio())
                // 맥락
                .context(ctx != null ? ContextInfo.builder()
                        .how(ctx.getContextHow())
                        .when(ctx.getContextWhen())
                        .why(ctx.getContextWhy())
                        .help(ctx.getContextHelp())
                        .build() : null)
                // 출처 (NPE 방지 처리)
                .source(src != null ? SourceInfo.builder()
                        .url(src.getSourceUrl())
                        .type(src.getSourceType() != null ? src.getSourceType().name() : null)
                        .timestamp(src.getTimestamp())
                        .build() : null)
                .build();
    }
}