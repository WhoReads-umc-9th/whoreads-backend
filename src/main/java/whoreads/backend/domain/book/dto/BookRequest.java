package whoreads.backend.domain.book.dto;

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
    @Size(max = 255, message = "제목은 255자 이내여야 합니다.") // 바꾼 이유: DB 컬럼 사이즈 초과 시 발생하는 500 에러를 막기 위해 길이 제한 추가
    private String title;

    @NotBlank(message = "작가 이름은 필수입니다.")
    @Size(max = 100, message = "작가 이름은 100자 이내여야 합니다.") // 바꾼 이유: 길이 제한 추가
    private String authorName;

    @URL(message = "올바른 URL 형식이어야 합니다.") // 바꾼 이유: 이상한 문자열이 링크로 들어오는 것을 방지
    private String coverUrl;

    @URL(message = "올바른 URL 형식이어야 합니다.") // 바꾼 이유: 올바른 URL 형식 확인
    private String link;

    private String genre;

    @Positive(message = "총 페이지 수는 0보다 커야 합니다.") // 바꾼 이유: 총 페이지 수가 0이거나 음수인 논리적 오류 차단
    private Integer totalPage;

    // DTO -> Entity 변환 메서드
    public Book toEntity() {
        return Book.builder()
                .title(title.trim()) // 바꾼 이유: 앞뒤 공백으로 인한 중복 데이터 생성 방지
                .authorName(authorName.trim())
                .coverUrl(coverUrl)
                .link(link)
                .genre(genre)
                .totalPage(totalPage)
                .build();
    }
}