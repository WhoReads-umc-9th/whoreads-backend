package whoreads.backend.domain.topic.entity;

import jakarta.persistence.*;
import lombok.*;
import whoreads.backend.domain.quote.entity.Quote;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
// 한 주제에 같은 인용이 중복해서 들어가지 않도록 유니크 제약조건 추가
@Table(name = "topic_quote", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"topic_id", "quote_id"})
})
public class TopicQuote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id", nullable = false)
    private Topic topic;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quote_id", nullable = false)
    private Quote quote;

    @Builder
    public TopicQuote(Topic topic, Quote quote) {
        this.topic = topic;
        this.quote = quote;
    }
}