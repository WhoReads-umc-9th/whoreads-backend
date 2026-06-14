package whoreads.backend.domain.book.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import whoreads.backend.domain.book.entity.Book;
import java.util.List;
import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long> {

    // 중복 체크용
    Optional<Book> findByTitleAndAuthorName(String title, String authorName);

    // 내부 검색용 (대소문자 무시, 제목 또는 작가 이름 포함)
    @Query("SELECT b FROM Book b WHERE LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(b.authorName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Book> searchByKeyword(@Param("keyword") String keyword);

    // TopicBook과 조인해서 해당 테마(TopicTag)를 가진 책들만 가져오기
    @Query("SELECT tb.book FROM TopicBook tb WHERE tb.topic.name = :theme")
    List<Book> findBooksByTheme(@Param("theme") whoreads.backend.domain.topic.entity.TopicTag theme, org.springframework.data.domain.Pageable pageable);
}