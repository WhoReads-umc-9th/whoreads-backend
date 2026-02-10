package whoreads.backend.domain.book.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import whoreads.backend.domain.book.entity.Book;
import whoreads.backend.domain.book.repository.BookQuoteRepository;
import whoreads.backend.domain.book.repository.BookRepository;

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
}