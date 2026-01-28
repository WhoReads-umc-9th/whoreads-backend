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

    private Long id; // 인용 ID
    private String originalText; // 인용 문구
    private int contextScore; // 맥락 점수

    // 책 정보 (인물 서재에서 볼 때 필요)
    private Long bookId;
    private String bookTitle;
    private String bookCover;

    // 인물 정보 (책 상세에서 볼 때 필요)
    private Long celebrityId;
    private String celebrityName;
    private String celebrityImg;
    private String celebrityJob; // 직업(한줄소개)

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
        private String type;
        private String timestamp;
    }

    // 데이터를 받아서 DTO로 변환하는 생성 메서드
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
                .celebrityJob(celebrity.getOneLineIntroduction())
                // 맥락
                .context(ctx != null ? ContextInfo.builder()
                        .how(ctx.getContextHow())
                        .when(ctx.getContextWhen())
                        .why(ctx.getContextWhy())
                        .help(ctx.getContextHelp())
                        .build() : null)
                // 출처
                .source(src != null ? SourceInfo.builder()
                        .url(src.getSourceUrl())
                        .type(src.getSourceType().toString())
                        .timestamp(src.getTimestamp())
                        .build() : null)
                .build();
    }
}