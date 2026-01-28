package whoreads.backend.domain.book.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import whoreads.backend.domain.book.entity.Book;

@Getter
@NoArgsConstructor
public class BookRequest {
    private String title;
    private String authorName;
    private String coverUrl;
    private String link;
    private String genre;

    // DTO -> Entity 변환 메서드
    public Book toEntity() {
        return Book.builder()
                .title(title)
                .authorName(authorName)
                .coverUrl(coverUrl)
                .link(link)
                .genre(genre)
                .build();
    }
}