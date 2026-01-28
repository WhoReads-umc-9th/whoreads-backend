package whoreads.backend.domain.book.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import whoreads.backend.domain.book.entity.Book;
import whoreads.backend.domain.book.entity.BookQuote;

import java.util.List;

public interface BookQuoteRepository extends JpaRepository<BookQuote, Long> {

    // 1. 책 기준 - 이 책에 달린 인용들 가져오기 (맥락 점수 높은 순)
    List<BookQuote> findByBookIdOrderByQuoteContextScoreDesc(Long bookId);

    // 2. 인물 기준 - 이 사람이 남긴 인용들 가져오기 (맥락 점수 높은 순)
    List<BookQuote> findByQuote_Celebrity_IdOrderByQuoteContextScoreDesc(Long celebrityId);

    // 3. 가장 많이 추천된 책 조회.
    // BookQuote들을 책별로 묶기 -> 개수(COUNT)가 많은 순서대로(DESC) 책만(bq.book) 반환
    @Query("SELECT bq.book FROM BookQuote bq GROUP BY bq.book ORDER BY COUNT(bq) DESC")
    List<Book> findMostRecommendedBooks(Pageable pageable);
}