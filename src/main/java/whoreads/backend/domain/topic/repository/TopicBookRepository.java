package whoreads.backend.domain.topic.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import whoreads.backend.domain.topic.entity.TopicBook;

// 주제별 도서 조회/권수 집계는 BookRepository(findBooksByTheme, countBooksByTheme)로 창구를 통일했다.
// 여기 있던 중복 조회 쿼리는 TopicService가 BookService로 위임하면서 쓰는 곳이 없어져 제거.
public interface TopicBookRepository extends JpaRepository<TopicBook, Long> {
}
