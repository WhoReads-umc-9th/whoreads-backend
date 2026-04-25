package whoreads.backend.domain.book.dto;

import tools.jackson.annotation.JsonInclude;
import tools.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
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
    @JsonProperty("author_name") private String authorName;
    private String genre;
    @JsonProperty("cover_url") private String coverUrl;
    private String link;
    @JsonProperty("quote_count") private int quoteCount;
    @Setter
    @JsonProperty("reading_info") private ReadingInfo readingInfo;
    private List<QuoteDetail> quotes;

    @Getter
    @Builder
    public static class QuoteDetail {
        @JsonProperty("quote_id") private Long quoteId;
        @JsonProperty("original_text") private String originalText;
        @JsonProperty("context_score") private int contextScore;
        private CelebrityInfo celebrity;
        private QuoteResponse.SourceInfo source;
    }

    @Getter
    @Builder
    public static class CelebrityInfo {
        private Long id;
        private String name;
        @JsonProperty("image_url") private String imageUrl;
        @JsonProperty("job_tags") private List<String> jobTags;

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
        @JsonProperty("reading_status") private ReadingStatus readingStatus;
        @JsonProperty("reading_page") private Integer readingPage;
        @JsonProperty("total_page") private Integer totalPage;
        @JsonProperty("started_at") private LocalDate startedAt;
        @JsonProperty("completed_at") private LocalDate completedAt;

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
