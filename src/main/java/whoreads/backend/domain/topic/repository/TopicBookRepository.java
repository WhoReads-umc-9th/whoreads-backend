package whoreads.backend.domain.topic.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import whoreads.backend.domain.topic.entity.Topic;
import whoreads.backend.domain.topic.entity.TopicBook;

import java.util.List;

public interface TopicBookRepository extends JpaRepository<TopicBook, Long> {

    // 특정 주제에 연결된 책들 조회 (Book 정보도 같이 가져옴 - N+1 방지)
    @Query("SELECT tb FROM TopicBook tb JOIN FETCH tb.book WHERE tb.topic = :topic")
    List<TopicBook> findByTopicWithFetchJoin(@Param("topic") Topic topic);
}