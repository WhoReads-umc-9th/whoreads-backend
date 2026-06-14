package whoreads.backend.domain.topic.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import whoreads.backend.domain.book.entity.Book;
import whoreads.backend.domain.topic.dto.TopicResponse;
import whoreads.backend.domain.topic.entity.Topic;
import whoreads.backend.domain.topic.entity.TopicBook;
import whoreads.backend.domain.topic.repository.TopicBookRepository;
import whoreads.backend.domain.topic.repository.TopicRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TopicService {

    private final TopicRepository topicRepository;
    private final TopicBookRepository topicBookRepository;

    public List<TopicResponse> getAllTopics() {
        List<Topic> topics = topicRepository.findAll();

        // 바꾼 이유: topics가 비어있을 때 빈 컬렉션으로 IN 쿼리를 날려 발생하는 에러(또는 불필요한 쿼리) 방지
        if (topics.isEmpty()) {
            return List.of();
        }
        // 변경: 안정적인 매핑을 위해 안전하게 참조 호출
        List<TopicBook> topicBooks = topicBookRepository.findAllByTopicInWithFetchJoin(topics);

        Map<Long, List<Book>> booksByTopicId = topicBooks.stream()
                .collect(Collectors.groupingBy(
                        tb -> tb.getTopic().getId(),
                        Collectors.mapping(TopicBook::getBook, Collectors.toList())
                ));

        return topics.stream()
                .map(topic -> TopicResponse.of(
                        topic,
                        booksByTopicId.getOrDefault(topic.getId(), List.of())
                ))
                .collect(Collectors.toList());
    }
}