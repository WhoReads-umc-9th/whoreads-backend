package whoreads.backend.domain.topic.entity;

import jakarta.persistence.*;
import lombok.*;
import whoreads.backend.domain.quote.entity.Quote;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "topic_quote")
public class TopicQuote { // 주제 - 인용 교차 테이블

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id")
    private Topic topic;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quote_id")
    private Quote quote;

    @Builder
    public TopicQuote(Topic topic, Quote quote) {
        this.topic = topic;
        this.quote = quote;
    }
}