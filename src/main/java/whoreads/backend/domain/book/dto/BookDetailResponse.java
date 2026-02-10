package whoreads.backend.domain.book.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import whoreads.backend.domain.book.entity.Book;
import whoreads.backend.domain.book.entity.BookQuote;
import whoreads.backend.domain.celebrity.entity.Celebrity;
import whoreads.backend.domain.celebrity.entity.CelebrityTag;
import whoreads.backend.domain.library.entity.UserBook;
import whoreads.backend.domain.library.enums.ReadingStatus;
import whoreads.backend.domain.quote.dto.QuoteResponse;
import whoreads.backend.domain.quote.entity.Quote;
import whoreads.backend.domain.quote.entity.QuoteSource;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
@Builder
public class BookDetailResponse {

    private Long id;
    private String title;
    private String authorName;
    private String genre;
    private String coverUrl;
    private String link;
    private int quoteCount;
    private ReadingInfo readingInfo;  // 담아둔 책이 아니면 null
    private List<QuoteDetail> quotes;

    @Getter
    @Builder
    public static class QuoteDetail {
        private Long quoteId;
        private String originalText;
        private int contextScore;
        private CelebrityInfo celebrity;
        private QuoteResponse.SourceInfo source;
    }

    @Getter
    @Builder
    public static class CelebrityInfo {
        private Long id;
        private String name;
        private String imageUrl;
        private List<String> jobTags;

        public static CelebrityInfo from(Celebrity celebrity) {
            return CelebrityInfo.builder()
                    .id(celebrity.getId())
                    .name(celebrity.getName())
                    .imageUrl(celebrity.getImageUrl())
                    .jobTags(celebrity.getJobTags().stream()
                            .map(CelebrityTag::getDescription)
                            .collect(Collectors.toList()))
                    .build();
        }
    }

    @Getter
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ReadingInfo {
        private ReadingStatus readingStatus;
        private Integer readingPage;
        private Integer totalPage;
        private LocalDate startedAt;
        private LocalDate completedAt;

        public static ReadingInfo from(UserBook userBook, Book book) {
            return ReadingInfo.builder()
                    .readingStatus(userBook.getReadingStatus())
                    .readingPage(userBook.getReadingPage())
                    .totalPage(book.getTotalPage())
                    .startedAt(userBook.getStartedAt())
                    .completedAt(userBook.getCompletedAt())
                    .build();
        }
    }

    public static BookDetailResponse of(
            Book book,
            List<BookQuote> bookQuotes,
            Map<Long, QuoteSource> sourceMap
    ) {
        List<QuoteDetail> quoteDetails = bookQuotes.stream()
                .map(bq -> {
                    Quote quote = bq.getQuote();
                    QuoteSource src = sourceMap.get(quote.getId());

                    return QuoteDetail.builder()
                            .quoteId(quote.getId())
                            .originalText(quote.getOriginalText())
                            .contextScore(quote.getContextScore())
                            .celebrity(CelebrityInfo.from(quote.getCelebrity()))
                            .source(src != null ? QuoteResponse.SourceInfo.builder()
                                    .url(src.getSourceUrl())
                                    .type(src.getSourceType() != null
                                            ? src.getSourceType().name() : null)
                                    .timestamp(src.getTimestamp())
                                    .build() : null)
                            .build();
                })
                .collect(Collectors.toList());

        return BookDetailResponse.builder()
                .id(book.getId())
                .title(book.getTitle())
                .authorName(book.getAuthorName())
                .genre(book.getGenre())
                .coverUrl(book.getCoverUrl())
                .link(book.getLink())
                .quoteCount(quoteDetails.size())
                .quotes(quoteDetails)
                .build();
    }
}
