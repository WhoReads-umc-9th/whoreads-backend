package whoreads.backend.domain.topic.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import whoreads.backend.domain.topic.entity.Topic;
import whoreads.backend.domain.topic.entity.TopicBook;

import java.util.List;

public interface TopicBookRepository extends JpaRepository<TopicBook, Long> {
    // 특정 주제(Topic)에 연결된 책들 다 가져오기
    List<TopicBook> findByTopic(Topic topic);
}