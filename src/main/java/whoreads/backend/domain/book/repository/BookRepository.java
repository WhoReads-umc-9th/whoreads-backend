package whoreads.backend.domain.book.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import whoreads.backend.domain.book.entity.Book;
import whoreads.backend.domain.topic.entity.TopicTag;

import java.util.List;
import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long> {

    // 중복 체크용
    Optional<Book> findByTitleAndAuthorName(String title, String authorName);

    // 내부 검색용 (대소문자 무시, 제목 또는 작가 이름 포함)
    // 바꾼 이유: 전체 결과를 한 번에 들고 오던 것을 페이징 조회로 변경 (목록 API 응답 속도 개선)
    @Query("SELECT b FROM Book b WHERE LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(b.authorName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Book> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    // TopicBook과 조인해서 해당 테마(TopicTag)를 가진 책들만 가져오기
    @Query("SELECT tb.book FROM TopicBook tb WHERE tb.topic.name = :theme")
    List<Book> findBooksByTheme(@Param("theme") TopicTag theme, Pageable pageable);
}