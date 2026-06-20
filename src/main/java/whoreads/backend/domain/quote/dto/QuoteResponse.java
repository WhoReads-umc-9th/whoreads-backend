package whoreads.backend.domain.quote.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JsonProperty("original_text") private String originalText;
    @JsonProperty("context_score") private int contextScore;

    @JsonProperty("book_id") private Long bookId;
    @JsonProperty("book_title") private String bookTitle;
    @JsonProperty("book_cover") private String bookCover;

    @JsonProperty("celebrity_id") private Long celebrityId;
    @JsonProperty("celebrity_name") private String celebrityName;
    @JsonProperty("celebrity_img") private String celebrityImg;
    @JsonProperty("celebrity_job") private String celebrityJob;

    private ContextInfo context;
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
        private String type;
        private String timestamp;
    }

    public static QuoteResponse of(Quote quote, Book book, Celebrity celebrity, QuoteContext ctx, QuoteSource src) {
        return QuoteResponse.builder()
                .id(quote.getId())
                .originalText(quote.getOriginalText())
                .contextScore(quote.getContextScore())
                .bookId(book.getId())
                .bookTitle(book.getTitle())
                .bookCover(book.getCoverUrl())
                .celebrityId(celebrity.getId())
                .celebrityName(celebrity.getName())
                .celebrityImg(celebrity.getImageUrl())
                .celebrityJob(celebrity.getShortBio())
                .context(ctx != null ? ContextInfo.builder()
                        .how(ctx.getContextHow())
                        .when(ctx.getContextWhen())
                        .why(ctx.getContextWhy())
                        .help(ctx.getContextHelp())
                        .build() : null)
                .source(src != null ? SourceInfo.builder()
                        .url(src.getSourceUrl())
                        .type(src.getSourceType() != null ? src.getSourceType().name() : null)
                        .timestamp(src.getTimestamp())
                        .build() : null)
                .build();
    }
}
