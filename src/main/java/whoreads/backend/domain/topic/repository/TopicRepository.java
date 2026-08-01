package whoreads.backend.domain.topic.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import whoreads.backend.domain.topic.entity.Topic;
import whoreads.backend.domain.topic.entity.TopicTag;

import java.util.Optional;

public interface TopicRepository extends JpaRepository<Topic, Long> {
    // findAll()은 기본 제공

    // 카테고리(TopicTag) 단건 필터 조회 - name이 unique 제약이므로 Optional 반환
    Optional<Topic> findByName(TopicTag name);
}