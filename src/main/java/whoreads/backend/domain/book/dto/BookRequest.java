package whoreads.backend.domain.book.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;
import lombok.Getter;
import lombok.NoArgsConstructor;
import whoreads.backend.domain.book.entity.Book;

@Getter
@NoArgsConstructor
public class BookRequest {

    @NotBlank(message = "제목은 필수입니다.")
    @Size(max = 255, message = "제목은 255자 이내여야 합니다.")
    private String title;

    @JsonProperty("author_name")
    @NotBlank(message = "작가 이름은 필수입니다.")
    @Size(max = 100, message = "작가 이름은 100자 이내여야 합니다.")
    private String authorName;

    @JsonProperty("cover_url")
    @URL(message = "올바른 URL 형식이어야 합니다.")
    private String coverUrl;

    @URL(message = "올바른 URL 형식이어야 합니다.")
    private String link;

    private String genre;

    @JsonProperty("total_page")
    @Positive(message = "총 페이지 수는 0보다 커야 합니다.")
    private Integer totalPage;

    public Book toEntity() {
        return Book.builder()
                .title(title.trim())
                .authorName(authorName.trim())
                .coverUrl(coverUrl)
                .link(link)
                .genre(genre)
                .totalPage(totalPage)
                .build();
    }
}
