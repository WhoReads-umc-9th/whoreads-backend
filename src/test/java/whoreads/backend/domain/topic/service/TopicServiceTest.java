package whoreads.backend.domain.topic.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import whoreads.backend.domain.book.entity.Book;
import whoreads.backend.domain.book.service.BookService;
import whoreads.backend.domain.topic.dto.TopicResponse;
import whoreads.backend.domain.topic.entity.Topic;
import whoreads.backend.domain.topic.entity.TopicTag;
import whoreads.backend.domain.topic.repository.TopicRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * 주제 목록(/api/topics)의 bookCount 는 previewSize 로 잘라낸 books.size() 가 아니라
 * 주제 전체 권수여야 하고, 목록/권수 모두 BookService 창구를 타서
 * /api/topics/{theme}/books 와 같은 결과가 나와야 한다.
 */
@ExtendWith(MockitoExtension.class)
class TopicServiceTest {

    @Mock
    private TopicRepository topicRepository;

    @Mock
    private BookService bookService;

    @InjectMocks
    private TopicService topicService;

    private static Book book(String title) {
        return Book.builder().title(title).authorName("작가").build();
    }

    @Test
    @DisplayName("previewSize가 있으면 books는 그 개수만큼만, bookCount는 주제 전체 권수를 내려준다")
    void getAllTopics_withPreviewSize_returnsPreviewBooksAndTotalCount() {
        Topic mindset = Topic.builder().name(TopicTag.MINDSET).build();
        given(topicRepository.findAll()).willReturn(List.of(mindset));
        given(bookService.getBooksByTheme(TopicTag.MINDSET, PageRequest.of(0, 3)))
                .willReturn(List.of(book("a"), book("b"), book("c")));
        given(bookService.countBooksByTheme(TopicTag.MINDSET)).willReturn(17L);

        List<TopicResponse> result = topicService.getAllTopics(null, 3);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTag()).isEqualTo(TopicTag.MINDSET);
        assertThat(result.get(0).getBooks()).hasSize(3);
        assertThat(result.get(0).getBookCount()).isEqualTo(17L);
    }

    @Test
    @DisplayName("previewSize가 없으면 주제 전체 도서를 페이징 없이 조회한다")
    void getAllTopics_withoutPreviewSize_fetchesUnpaged() {
        given(topicRepository.findAll()).willReturn(List.of(Topic.builder().name(TopicTag.SOCIETY).build()));
        given(bookService.getBooksByTheme(TopicTag.SOCIETY, Pageable.unpaged())).willReturn(List.of(book("a"), book("b")));
        given(bookService.countBooksByTheme(TopicTag.SOCIETY)).willReturn(2L);

        List<TopicResponse> result = topicService.getAllTopics(null, null);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(bookService).getBooksByTheme(org.mockito.ArgumentMatchers.eq(TopicTag.SOCIETY), pageableCaptor.capture());
        assertThat(pageableCaptor.getValue().isUnpaged()).isTrue();
        assertThat(result.get(0).getBooks()).hasSize(2);
        assertThat(result.get(0).getBookCount()).isEqualTo(2L);
    }

    @Test
    @DisplayName("tag로 필터하면 해당 주제 하나만 조회하고, DB에 없는 주제면 빈 목록을 반환한다")
    void getAllTopics_withTag_filtersSingleTopic() {
        Topic turningPoint = Topic.builder().name(TopicTag.TURNING_POINT).build();
        given(topicRepository.findByName(TopicTag.TURNING_POINT)).willReturn(Optional.of(turningPoint));
        given(topicRepository.findByName(TopicTag.TOP_20)).willReturn(Optional.empty());
        given(bookService.getBooksByTheme(TopicTag.TURNING_POINT, PageRequest.of(0, 1))).willReturn(List.of(book("a")));
        given(bookService.countBooksByTheme(TopicTag.TURNING_POINT)).willReturn(5L);

        List<TopicResponse> found = topicService.getAllTopics(TopicTag.TURNING_POINT, 1);
        List<TopicResponse> missing = topicService.getAllTopics(TopicTag.TOP_20, 1);

        assertThat(found).extracting(TopicResponse::getBookCount).containsExactly(5L);
        assertThat(missing).isEmpty();
        verify(topicRepository, never()).findAll();
    }
}
