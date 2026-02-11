package whoreads.backend.domain.topic.dto;

import lombok.Builder;
import lombok.Getter;
import whoreads.backend.domain.book.dto.BookResponse;
import whoreads.backend.domain.book.entity.Book;
import whoreads.backend.domain.topic.entity.Topic;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class TopicResponse {
    private Long id;
    private String name; // ENUM의 description ("삶의 방향" 등)
    private List<BookResponse> books;

    public static TopicResponse of(Topic topic, List<Book> books) {
        return TopicResponse.builder()
                .id(topic.getId())
                .name(topic.getName().getDescription()) // ENUM에서 한글 명칭 추출
                .books(books.stream()
                        .map(BookResponse::from)
                        .collect(Collectors.toList()))
                .build();
    }
}