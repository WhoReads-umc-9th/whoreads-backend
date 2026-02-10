package whoreads.backend.domain.book.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import whoreads.backend.domain.book.entity.Book;
import whoreads.backend.domain.book.entity.BookQuote;

import java.util.List;
import java.util.Optional;

public interface BookQuoteRepository extends JpaRepository<BookQuote, Long> {

    // 1. 책 기준 - 이 책에 달린 인용들 가져오기 (N+1 방지: Quote와 Celebrity 함께 로딩)
    @Query("SELECT bq FROM BookQuote bq JOIN FETCH bq.quote q JOIN FETCH q.celebrity WHERE bq.book.id = :bookId ORDER BY q.contextScore DESC")
    List<BookQuote> findByBookIdWithFetchJoin(@Param("bookId") Long bookId);

    // 2. 인물 기준 - 이 사람이 남긴 인용들 가져오기 (N+1 방지)
    @Query("SELECT bq FROM BookQuote bq JOIN FETCH bq.book WHERE bq.quote.celebrity.id = :celebrityId ORDER BY bq.quote.contextScore DESC")
    List<BookQuote> findByCelebrityIdWithFetchJoin(@Param("celebrityId") Long celebrityId);

    // 3. 가장 많이 추천된 책 조회
    @Query("SELECT bq.book FROM BookQuote bq GROUP BY bq.book ORDER BY COUNT(bq) DESC")
    List<Book> findMostRecommendedBooks(Pageable pageable);

    // 4. 인용(Quote) ID로 연결된 책(BookQuote) 찾기
    // (주제별 큐레이션 등에서 인용 정보만 가지고 있을 때, 역으로 책 정보를 알아내기 위해 사용)
    Optional<BookQuote> findByQuoteId(Long quoteId);
}