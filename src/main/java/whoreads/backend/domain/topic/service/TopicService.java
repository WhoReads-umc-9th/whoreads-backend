package whoreads.backend.domain.topic.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import whoreads.backend.domain.book.dto.BookResponse;
import whoreads.backend.domain.book.entity.Book;
import whoreads.backend.domain.book.service.BookService;
import whoreads.backend.domain.topic.dto.TopicResponse;
import whoreads.backend.domain.topic.entity.Topic;
import whoreads.backend.domain.topic.entity.TopicTag;
import whoreads.backend.domain.topic.repository.TopicRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TopicService {

    private final TopicRepository topicRepository;
    // 바꾼 이유: 주제별 도서 조회 로직이 BookService와 중복되어 있어, 구현은 BookService 한 곳만 두고 위임하도록 정리
    private final BookService bookService;

    public List<TopicResponse> getAllTopics(TopicTag tag, Integer previewSize) {
        // tag가 있으면 해당 카테고리만, 없으면 전체 조회
        List<Topic> topics = (tag == null)
                ? topicRepository.findAll()
                : topicRepository.findByName(tag).map(List::of).orElseGet(List::of);

        // previewSize가 있으면 주제별로 그 개수만큼만 조회 (메인 배너 미리보기용, DB 단에서 LIMIT)
        // 없으면 기존처럼 주제별 전체 도서 반환 (하위호환)
        Pageable pageable = (previewSize == null)
                ? Pageable.unpaged()
                : PageRequest.of(0, previewSize);

        // 바꾼 이유: 주제 목록만 topic_book을 직접 읽어서, 같은 주제를 /api/topics/{theme}/books 로 조회할 때와
        // 결과가 달랐다(TOP_20은 topic_book 매핑이 없어 목록에서는 빈 배열). 도서 목록과 권수(bookCount)를
        // 모두 BookService 창구로 통일해서 어느 엔드포인트로 보든 같은 책, 같은 개수가 나오게 했다.
        return topics.stream()
                .map(topic -> TopicResponse.of(
                        topic,
                        bookService.getBooksByTheme(topic.getName(), pageable),
                        bookService.countBooksByTheme(topic.getName())
                ))
                .collect(Collectors.toList());
    }

    public List<BookResponse> getBooksByTopic(TopicTag theme, Pageable pageable) {
        // 바꾼 이유: 직접 조회하면 TOP_20처럼 topic_book 매핑이 없는 주제가 빈 배열로 내려감.
        // BookService에 위임해서 /api/books/themes/{theme}와 항상 같은 결과가 나오도록 통일
        List<Book> books = bookService.getBooksByTheme(theme, pageable);

        return books.stream()
                .map(BookResponse::from)
                .collect(Collectors.toList());
    }
}
