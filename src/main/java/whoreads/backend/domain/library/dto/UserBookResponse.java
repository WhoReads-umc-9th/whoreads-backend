package whoreads.backend.domain.library.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import whoreads.backend.domain.book.dto.BookResponse;
import whoreads.backend.domain.library.entity.UserBook;

import java.util.List;

public class UserBookResponse {

    @Getter
    @Builder
    public static class Summary {
        @JsonProperty("completed_count") private Integer completedCount;
        @JsonProperty("reading_count") private Integer readingCount;
        @JsonProperty("total_read_minutes") private Long totalReadMinutes;
    }

    @Getter
    @Builder
    public static class AddResult {
        @JsonProperty("user_book_id") private Long userBookId;
    }

    @Getter
    @Builder
    public static class CelebritySummary {
        private Long id;
        @JsonProperty("profile_url") private String profileUrl;
    }

    @Getter
    @Builder
    public static class SimpleBook {
        @JsonProperty("user_book_id") private Long userBookId;
        private BookResponse book;
        @JsonProperty("reading_page") private Integer readingPage;
        @JsonProperty("celebrities_count") private Integer celebritiesCount;
        private List<CelebritySummary> celebrities;

        public static SimpleBook from(UserBook userBook, List<CelebritySummary> celebrities) {
            return SimpleBook.builder()
                    .userBookId(userBook.getId())
                    .book(BookResponse.from(userBook.getBook()))
                    .readingPage(userBook.getReadingPage())
                    .celebritiesCount(celebrities.size())
                    .celebrities(celebrities)
                    .build();
        }
    }

    @Getter
    @Builder
    public static class BookList {
        private List<SimpleBook> books;
        @JsonProperty("next_cursor") private Long nextCursor;
        @JsonProperty("has_next") private Boolean hasNext;
    }
}
