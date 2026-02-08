package whoreads.backend.domain.topic.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import whoreads.backend.domain.topic.entity.Topic;

public interface TopicRepository extends JpaRepository<Topic, Long> {
    // findAll()은 기본 제공
}