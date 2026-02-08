package whoreads.backend.domain.topic.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import whoreads.backend.domain.topic.entity.Topic;
import whoreads.backend.domain.topic.entity.TopicQuote;

import java.util.List;

public interface TopicQuoteRepository extends JpaRepository<TopicQuote, Long> {

    @Query("SELECT tq FROM TopicQuote tq " +
            "JOIN FETCH tq.quote q " +
            "JOIN FETCH q.celebrity " +
            "WHERE tq.topic = :topic " +
            "ORDER BY q.contextScore DESC")
    List<TopicQuote> findByTopicWithFetchJoin(@Param("topic") Topic topic);
}