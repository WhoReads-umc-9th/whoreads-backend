package whoreads.backend.domain.topic.dto;

import lombok.Builder;
import lombok.Getter;
import whoreads.backend.domain.quote.dto.QuoteResponse;
import whoreads.backend.domain.topic.entity.Topic;
import whoreads.backend.domain.topic.entity.TopicTag;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class TopicResponse {
    private Long id;
    private String name;        // 주제명
    private String description; // 부제목(설명)
    private List<String> tags;  // 태그 리스트
    private List<QuoteResponse> quotes;

    public static TopicResponse of(Topic topic, List<QuoteResponse> quotes) {
        return TopicResponse.builder()
                .id(topic.getId())
                .name(topic.getName())
                .description(topic.getDescription())
                .tags(topic.getTags().stream()
                        .map(TopicTag::getDescription)
                        .collect(Collectors.toList()))
                .quotes(quotes)
                .build();
    }
}