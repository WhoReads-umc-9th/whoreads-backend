package whoreads.backend.domain.topic.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import whoreads.backend.domain.book.entity.Book;
import whoreads.backend.domain.topic.entity.Topic;
import whoreads.backend.domain.topic.entity.TopicBook;
import whoreads.backend.domain.topic.entity.TopicTag;

import java.util.List;

public interface TopicBookRepository extends JpaRepository<TopicBook, Long> {

    // 특정 주제에 연결된 책들 조회 (Book 정보도 같이 가져옴 - N+1 방지)
    @Query("SELECT tb FROM TopicBook tb JOIN FETCH tb.book WHERE tb.topic = :topic")
    List<TopicBook> findByTopicWithFetchJoin(@Param("topic") Topic topic);
    
    // TopicBook과 연관된 Topic을 조인해서 Enum(TopicTag) 이름으로 바로 필터링하고, Book만 가져옴
    @Query("SELECT tb.book FROM TopicBook tb JOIN tb.topic t WHERE t.name = :theme")
    List<Book> findBooksByThemeName(@Param("theme") TopicTag theme, Pageable pageable);
}