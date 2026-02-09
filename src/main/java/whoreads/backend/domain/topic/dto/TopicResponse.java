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
    private String name;
    // description 삭제
    private List<String> tags;
    private List<BookResponse> books;

    public static TopicResponse of(Topic topic, List<Book> books) {
        return TopicResponse.builder()
                .id(topic.getId())
                .name(topic.getName())
                .tags(topic.getTags().stream()
                        .map(TopicTag::getDescription)
                        .collect(Collectors.toList()))
                .books(books.stream()
                        .map(BookResponse::from)
                        .collect(Collectors.toList()))
                .build();
    }
}