package whoreads.backend.domain.book.dto;

import lombok.Builder;
import lombok.Getter;
import whoreads.backend.domain.book.entity.Book;

@Getter
@Builder
public class BookResponse {
    private Long id;
    private String title;
    private String authorName;
    private String coverUrl;
    private String link;
    private String genre;
    private Integer pageCount;

    public static BookResponse from(Book book) {
        return BookResponse.builder()
                .id(book.getId())
                .title(book.getTitle())
                .authorName(book.getAuthorName())
                .coverUrl(book.getCoverUrl())
                .link(book.getLink())
                .genre(book.getGenre())
                .pageCount(book.getPageCount())
                .build();
    }
}