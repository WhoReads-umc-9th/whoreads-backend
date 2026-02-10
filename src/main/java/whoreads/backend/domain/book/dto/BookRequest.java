package whoreads.backend.domain.book.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import whoreads.backend.domain.book.entity.Book;

@Getter
@NoArgsConstructor
public class BookRequest {

    @NotBlank(message = "제목은 필수입니다.")
    private String title;

    @NotBlank(message = "작가 이름은 필수입니다.")
    private String authorName;

    private String coverUrl;
    private String link;
    private String genre;
    private Integer totalPage;

    // DTO -> Entity 변환 메서드
    public Book toEntity() {
        return Book.builder()
                .title(title)
                .authorName(authorName)
                .coverUrl(coverUrl)
                .link(link)
                .genre(genre)
                .totalPage(totalPage)
                .build();
    }
}
