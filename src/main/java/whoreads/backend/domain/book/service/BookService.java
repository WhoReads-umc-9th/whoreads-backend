package whoreads.backend.domain.book.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import whoreads.backend.domain.book.entity.Book;
import whoreads.backend.domain.book.repository.BookQuoteRepository;
import whoreads.backend.domain.book.repository.BookRepository;
import org.springframework.data.domain.PageRequest;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookService {

    private final BookRepository bookRepository;
    private final BookQuoteRepository bookQuoteRepository;

    // 책 상세 조회
    public Book getBook(Long bookId) {
        return bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("책을 찾을 수 없습니다."));
    }

    public List<Book> getAllBooks(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return bookRepository.findAll();
        }
        // 제목이나 작가에 키워드가 있으면 검색
        return bookRepository.findByTitleContainingOrAuthorNameContaining(keyword, keyword);
    }

    // 어드민용 책 등록
    // 제목과 작가가 DB에 이미 있으면 저장하지 않고 기존 책 반환
    @Transactional
    public Book registerBook(Book book) {
        return bookRepository.findByTitleAndAuthorName(book.getTitle(), book.getAuthorName())
                .orElseGet(() -> bookRepository.save(book));
    }

    // 가장 많이 추천된 책 TOP N 조회
    public List<Book> getMostRecommendedBooks(int limit) {
        // PageRequest.of(페이지번호, 개수) -> 0페이지부터 limit개 가져와라
        return bookQuoteRepository.findMostRecommendedBooks(PageRequest.of(0, limit));
    }
}