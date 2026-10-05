package whoreads.backend.domain.topic.dto;

import lombok.Builder;
import lombok.Getter;
import whoreads.backend.domain.book.dto.BookResponse;
import whoreads.backend.domain.book.entity.Book;
import whoreads.backend.domain.topic.entity.Topic;
import whoreads.backend.domain.topic.entity.TopicTag;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class TopicResponse {
    private Long id;

    // 추가 이유: 기존에는 한글 name만 내려줘서 프론트가 /api/topics/{theme}/books 에 넣을 값을
    // 응답에서 알 수 없었다(id나 한글을 넣으면 전부 변환 실패). 주제별 도서 조회에 쓸 값은 이 tag 다.
    private TopicTag tag; // ENUM 상수명 ("MINDSET" 등)

    private String name; // ENUM의 description ("삶의 방향" 등)
    private List<BookResponse> books;

    public static TopicResponse of(Topic topic, List<Book> books) {
        return TopicResponse.builder()
                .id(topic.getId())
                .tag(topic.getName())
                .name(topic.getName().getDescription()) // ENUM에서 한글 명칭 추출
                .books(books.stream()
                        .map(BookResponse::from)
                        .collect(Collectors.toList()))
                .build();
    }
}
