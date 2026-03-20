package whoreads.backend.domain.book.service;

import org.springframework.data.domain.PageRequest; // 바꾼 이유: PageRequest 임포트 추가 (페이징 처리 에러 해결)
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

    public List<Book> getAllBooks(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return bookRepository.findAll();
        }
        return bookRepository.searchByKeyword(keyword.trim());
    }

    public List<Book> getMostRecommendedBooks(int limit) {
        // 바꾼 이유: limit 숫자 하나만 넣으면 에러가 나므로, PageRequest 객체로 생성해서 넘겨줌
        return bookQuoteRepository.findMostRecommendedBooks(PageRequest.of(0, limit));
    }

    // 주제별 책 조회 로직
    public List<Book> getBooksByTheme(TopicTag theme, int limit) {
        // 프론트에서 TOP_20을 요청했을 땐 기존 로직 재사용
        if (theme == TopicTag.TOP_20) {
            return getMostRecommendedBooks(limit);
        }
        // 그 외의 주제들은 Topic... (기존 로직 유지)
        return Collections.emptyList();
    }
}