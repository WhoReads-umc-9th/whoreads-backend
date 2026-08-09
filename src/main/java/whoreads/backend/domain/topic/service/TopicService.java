package whoreads.backend.domain.topic.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import whoreads.backend.domain.book.dto.BookResponse;
import whoreads.backend.domain.book.entity.Book;
import whoreads.backend.domain.topic.dto.TopicResponse;
import whoreads.backend.domain.topic.entity.Topic;
import whoreads.backend.domain.topic.entity.TopicBook;
import whoreads.backend.domain.topic.entity.TopicTag;
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

    public List<TopicResponse> getAllTopics(TopicTag tag, Integer previewSize) {
        // tag가 있으면 해당 카테고리만, 없으면 전체 조회
        List<Topic> topics = (tag == null)
                ? topicRepository.findAll()
                : topicRepository.findByName(tag).map(List::of).orElseGet(List::of);

        // 바꾼 이유: topics가 비어있을 때 빈 컬렉션으로 IN 쿼리를 날려 발생하는 에러(또는 불필요한 쿼리) 방지
        if (topics.isEmpty()) {
            return List.of();
        }

        // previewSize가 있으면 주제별로 그 개수만큼만 조회 (메인 배너 미리보기용, DB 단에서 LIMIT)
        // 없으면 기존처럼 주제별 전체 도서 반환 (하위호환)
        if (previewSize != null) {
            Pageable preview = PageRequest.of(0, previewSize);
            return topics.stream()
                    .map(topic -> TopicResponse.of(
                            topic,
                            topicBookRepository.findBooksByThemeName(topic.getName(), preview)
                    ))
                    .collect(Collectors.toList());
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

    public List<BookResponse> getBooksByTopic(TopicTag theme, Pageable pageable) {
        List<Book> books = topicBookRepository.findBooksByThemeName(theme, pageable);

        return books.stream()
                .map(BookResponse::from)
                .collect(Collectors.toList());
    }
}