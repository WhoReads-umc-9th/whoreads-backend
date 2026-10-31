package whoreads.backend.domain.book.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import whoreads.backend.domain.book.repository.BookQuoteRepository;
import whoreads.backend.domain.book.repository.BookRepository;
import whoreads.backend.domain.topic.entity.TopicTag;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private BookQuoteRepository bookQuoteRepository;

    @InjectMocks
    private BookService bookService;

    @Test
    @DisplayName("일반 주제의 권수는 topic_book 매핑 수로 센다")
    void countBooksByTheme_regularTopic_countsTopicBookMappings() {
        given(bookRepository.countBooksByTheme(TopicTag.MINDSET)).willReturn(12L);

        assertThat(bookService.countBooksByTheme(TopicTag.MINDSET)).isEqualTo(12L);
        verify(bookQuoteRepository, never()).countDistinctBooks();
    }

    @Test
    @DisplayName("TOP_20 권수는 topic_book이 아니라 인용이 달린 책 수로 센다 (목록 조회 기준과 동일)")
    void countBooksByTheme_top20_countsBooksWithQuotes() {
        given(bookQuoteRepository.countDistinctBooks()).willReturn(43L);

        assertThat(bookService.countBooksByTheme(TopicTag.TOP_20)).isEqualTo(43L);
        verify(bookRepository, never()).countBooksByTheme(any());
    }
}
