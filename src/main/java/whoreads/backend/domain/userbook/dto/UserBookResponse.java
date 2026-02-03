package whoreads.backend.domain.userbook.dto;

import lombok.Builder;
import lombok.Getter;
import whoreads.backend.domain.userbook.entity.UserBook;
import whoreads.backend.domain.userbook.enums.ReadingStatus;

import java.util.List;

public class UserBookResponse {

    @Getter
    @Builder
    public static class Summary {
        private Integer completedNumber;
        private Integer readingNumber;
        private Long readTime;
    }

    @Getter
    @Builder
    public static class SimpleBook {
        private Long bookId;
        private String bookTitle;
        private String bookAuthor;
        private String coverUrl;
        private Integer readingPage;
        private Integer totalPage;

        public static SimpleBook from(UserBook userBook) {
            return SimpleBook.builder()
                    .bookId(userBook.getBook().getId())
                    .bookTitle(userBook.getBook().getTitle())
                    .bookAuthor(userBook.getBook().getAuthorName())
                    .coverUrl(userBook.getBook().getCoverUrl())
                    .readingPage(userBook.getReadingPage())
                    .totalPage(userBook.getBook().getTotalPage())
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

    @Getter
    @Builder
    public static class Detail {
        private Long userBookId;
        private Long bookId;
        private String bookTitle;
        private String bookAuthor;
        private String coverUrl;
        private ReadingStatus readingStatus;
        private Integer readingPage;
        private Integer totalPage;

        public static Detail from(UserBook userBook) {
            return Detail.builder()
                    .userBookId(userBook.getId())
                    .bookId(userBook.getBook().getId())
                    .bookTitle(userBook.getBook().getTitle())
                    .bookAuthor(userBook.getBook().getAuthorName())
                    .coverUrl(userBook.getBook().getCoverUrl())
                    .readingStatus(userBook.getReadingStatus())
                    .readingPage(userBook.getReadingPage())
                    .totalPage(userBook.getBook().getTotalPage())
                    .build();
        }
    }
}
