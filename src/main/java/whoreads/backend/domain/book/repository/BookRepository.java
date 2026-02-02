package whoreads.backend.domain.book.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import whoreads.backend.domain.book.entity.Book;
import java.util.List;
import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long> {
    // 중복 체크, ISBN << 사용하는건 어떤지 체크
    Optional<Book> findByTitleAndAuthorName(String title, String authorName);

    // 내부 검색용 (제목 또는 작가 이름에 키워드 포함)
    List<Book> findByTitleContainingOrAuthorNameContaining(String titleKeyword, String authorKeyword);
}