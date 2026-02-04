package whoreads.backend.domain.library.dto;

import lombok.Builder;
import lombok.Getter;
import whoreads.backend.domain.book.dto.BookResponse;
import whoreads.backend.domain.library.entity.UserBook;

import java.util.List;

public class UserBookResponse {

    @Getter
    @Builder
    public static class Summary {
        private Integer completedCount;
        private Integer readingCount;
        private Long totalReadMinutes;
    }

    @Getter
    @Builder
    public static class AddResult {
        private Long userBookId;
    }

    @Getter
    @Builder
    public static class CelebritySummary {
        private Long id;
        private String profileUrl;
    }

    @Getter
    @Builder
    public static class SimpleBook {
        private BookResponse book;
        private Integer readingPage;
        private Integer celebritiesCount;
        private List<CelebritySummary> celebrities;

        public static SimpleBook from(UserBook userBook, List<CelebritySummary> celebrities) {
            return SimpleBook.builder()
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
        private Long nextCursor;
        private Boolean hasNext;
    }

}
