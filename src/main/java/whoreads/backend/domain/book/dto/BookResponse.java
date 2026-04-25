package whoreads.backend.domain.book.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import whoreads.backend.domain.book.entity.Book;

@Getter
@Builder
public class BookResponse {
    private Long id;
    private String title;
    @JsonProperty("author_name") private String authorName;
    private String genre;
    @JsonProperty("cover_url") private String coverUrl;
    @JsonProperty("total_page") private Integer totalPage;

    public static BookResponse from(Book book) {
        return BookResponse.builder()
                .id(book.getId())
                .title(book.getTitle())
                .authorName(book.getAuthorName())
                .genre(book.getGenre())
                .coverUrl(book.getCoverUrl())
                .totalPage(book.getTotalPage())
                .build();
    }
}
