package whoreads.backend.domain.book.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest; // 바꾼 이유: PageRequest 임포트 추가 (페이징 처리 에러 해결)
import org.springframework.data.domain.Pageable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import whoreads.backend.domain.book.dto.BookDetailResponse;
import whoreads.backend.domain.book.dto.BookRequest;
import whoreads.backend.domain.book.entity.Book;
import whoreads.backend.domain.book.entity.BookQuote;
import whoreads.backend.domain.book.repository.BookQuoteRepository;
import whoreads.backend.domain.book.repository.BookRepository;
import whoreads.backend.domain.library.repository.UserBookRepository;
import whoreads.backend.domain.quote.entity.QuoteSource;
import whoreads.backend.domain.quote.repository.QuoteSourceRepository;
import whoreads.backend.domain.topic.entity.TopicTag;
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

    @Transactional
    public Book registerBook(BookRequest request) {
        String trimmedTitle = request.getTitle().trim();
        String trimmedAuthor = request.getAuthorName().trim();

        return bookRepository.findByTitleAndAuthorName(trimmedTitle, trimmedAuthor)
                .orElseGet(() -> bookRepository.save(request.toEntity()));
    }

    public BookDetailResponse getBookDetail(Long bookId, Long memberId) {
        // 바꾼 이유: EntityNotFoundException 대신 직접 정의한 CustomException과 ErrorCode를 사용하도록 통일
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new CustomException(ErrorCode.BOOK_NOT_FOUND));

        // 인용 + 유명인 JOIN FETCH (contextScore DESC 정렬)
        List<BookQuote> bookQuotes = bookQuoteRepository.findByBookIdWithEntityGraph(bookId);

        // 출처 배치 조회
        List<Long> quoteIds = bookQuotes.stream()
                .map(bq -> bq.getQuote().getId())
                .collect(Collectors.toList());

        Map<Long, QuoteSource> sourceMap = quoteIds.isEmpty()
                ? Collections.emptyMap()
                : quoteSourceRepository.findByQuoteIdIn(quoteIds).stream()
                .collect(Collectors.toMap(
                        src -> src.getQuote().getId(),
                        Function.identity(),
                        // 바꾼 이유: 한 인용에 출처가 둘 이상이면 toMap이 IllegalStateException을 던져
                        // 책 상세 전체가 500으로 죽음. 먼저 조회된 것을 쓰고 넘어가도록 함
                        (first, duplicate) -> first
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

    /**
     * 도서 목록 조회 / 검색.
     * 바꾼 이유: 전체 도서를 매번 통째로 조회해서 목록 API가 느렸음. 페이징으로 필요한 만큼만 가져오도록 변경.
     */
    public Page<Book> getAllBooks(String keyword, Pageable pageable) {
        if (keyword == null || keyword.isBlank()) {
            return bookRepository.findAll(pageable);
        }
        return bookRepository.searchByKeyword(keyword.trim(), pageable);
    }

    public List<Book> getMostRecommendedBooks(int limit) {
        // 바꾼 이유: TOP_20도 결국 "주제별 도서 조회"의 한 종류라 getBooksByTheme으로 창구를 합침
        return getBooksByTheme(TopicTag.TOP_20, limit);
    }

    /**
     * 주제별 책 조회 - 주제 관련 API(/api/books/themes, /api/topics/{theme}/books,
     * /api/books/most-recommended)가 모두 이 메서드를 타도록 창구를 하나로 통일했다.
     * 바꾼 이유: 같은 기능이 여러 곳에 흩어져 있어 엔드포인트마다 결과가 달랐음
     * (예: TOP_20을 /api/topics 쪽으로 요청하면 topic_book 매핑이 없어 빈 배열이 내려갔음).
     */
    public List<Book> getBooksByTheme(TopicTag theme, Pageable pageable) {
        // TOP_20은 topic_book 매핑이 아니라 인용 수 집계로 뽑는 가상 주제
        if (theme == TopicTag.TOP_20) {
            return bookQuoteRepository.findMostRecommendedBooks(pageable);
        }
        return bookRepository.findBooksByTheme(theme, pageable);
    }

    public List<Book> getBooksByTheme(TopicTag theme, int limit) {
        if (limit <= 0) {
            throw new IllegalArgumentException("limit must be positive");
        }
        // 바꾼 이유: limit 숫자 하나만 넣으면 에러가 나므로, PageRequest 객체로 생성해서 넘겨줌
        return getBooksByTheme(theme, PageRequest.of(0, limit));
    }
}