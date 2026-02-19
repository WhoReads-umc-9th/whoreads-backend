package whoreads.backend.domain.book.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import whoreads.backend.domain.book.dto.BookDetailResponse;
import whoreads.backend.domain.book.entity.Book;
import whoreads.backend.domain.book.entity.BookQuote;
import whoreads.backend.domain.book.repository.BookQuoteRepository;
import whoreads.backend.domain.book.repository.BookRepository;
import whoreads.backend.domain.library.repository.UserBookRepository;
import whoreads.backend.domain.quote.entity.QuoteSource;
import whoreads.backend.domain.quote.repository.QuoteSourceRepository;
import whoreads.backend.domain.topic.entity.TopicTag;
import whoreads.backend.domain.topic.repository.TopicBookRepository;
import whoreads.backend.global.exception.CustomException;
import whoreads.backend.global.exception.ErrorCode;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookService {

    private final BookRepository bookRepository;
    private final BookQuoteRepository bookQuoteRepository;
    private final QuoteSourceRepository quoteSourceRepository;
    private final UserBookRepository userBookRepository;
    private final TopicBookRepository topicBookRepository;

    // 책 상세 조회
    public Book getBook(Long bookId) {
        return bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("책을 찾을 수 없습니다. ID=" + bookId));
    }

    public List<Book> getAllBooks(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return bookRepository.findAll();
        }
        return bookRepository.searchByKeyword(keyword);
    }

    // 어드민용 책 등록 (동시성 문제 해결)
    @Transactional
    public Book registerBook(Book book) {
        return bookRepository.findByTitleAndAuthorName(book.getTitle(), book.getAuthorName())
                .orElseGet(() -> {
                    try {
                        return bookRepository.save(book);
                    } catch (DataIntegrityViolationException e) {
                        // 동시에 누군가 저장했다면 다시 조회해서 반환
                        return bookRepository.findByTitleAndAuthorName(book.getTitle(), book.getAuthorName())
                                .orElseThrow(() -> new IllegalStateException("책 저장 중 알 수 없는 오류 발생"));
                    }
                });
    }

    // 가장 많이 추천된 책 TOP N 조회
    public List<Book> getMostRecommendedBooks(int limit) {
        return bookQuoteRepository.findMostRecommendedBooks(PageRequest.of(0, limit));
    }

    // 책 상세페이지 조회
    public BookDetailResponse getBookDetail(Long bookId, Long memberId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new CustomException(ErrorCode.BOOK_NOT_FOUND));

        // 인용 + 유명인 JOIN FETCH (contextScore DESC 정렬)
        List<BookQuote> bookQuotes = bookQuoteRepository.findByBookIdWithFetchJoin(bookId);

        // 출처 배치 조회
        List<Long> quoteIds = bookQuotes.stream()
                .map(bq -> bq.getQuote().getId())
                .collect(Collectors.toList());

        Map<Long, QuoteSource> sourceMap = quoteIds.isEmpty()
                ? Collections.emptyMap()
                : quoteSourceRepository.findByQuoteIdIn(quoteIds).stream()
                        .collect(Collectors.toMap(
                                src -> src.getQuote().getId(),
                                Function.identity()
                        ));

        // 응답 조립
        BookDetailResponse response = BookDetailResponse.of(book, bookQuotes, sourceMap);

        // 로그인 사용자의 읽기 상태 확인
        if (memberId != null) {
            userBookRepository.findByMemberIdAndBookId(memberId, bookId)
                    .ifPresent(userBook -> response.setReadingInfo(
                            BookDetailResponse.ReadingInfo.from(userBook, book)
                    ));
        }

        return response;
    }

    // 주제별 책 조회 로직
    public List<Book> getBooksByTheme(TopicTag theme, int limit) {
        // 프론트에서 TOP_20을 요청했을 땐 기존 로직 재사용
        if (theme == TopicTag.TOP_20) {
            return getMostRecommendedBooks(limit);
        }

        // 그 외의 주제들은 TopicBook 매핑 테이블에서 조회
        return topicBookRepository.findBooksByThemeName(theme, PageRequest.of(0, limit));
    }
}