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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TopicService {

    private final TopicRepository topicRepository;
    private final TopicBookRepository topicBookRepository;

    public List<TopicResponse> getAllTopics() {
        List<Topic> topics = topicRepository.findAll();
        List<TopicResponse> responses = new ArrayList<>();

        for (Topic topic : topics) {
            // 해당 주제에 연결된 책들 가져오기 (Fetch Join)
            List<Book> books = topicBookRepository.findByTopicWithFetchJoin(topic).stream()
                    .map(TopicBook::getBook)
                    .collect(Collectors.toList());

            // DTO 변환
            responses.add(TopicResponse.of(topic, books));
        }

        return responses;
    }
}