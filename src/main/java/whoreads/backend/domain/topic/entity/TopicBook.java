package whoreads.backend.domain.topic.entity;

import jakarta.persistence.*;
import lombok.*;
import whoreads.backend.domain.book.entity.Book;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "topic_book")
public class TopicBook { // 주제 - 책 교차 테이블

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id")
    private Book book;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id")
    private Topic topic;

    @Builder
    public TopicBook(Book book, Topic topic) {
        this.book = book;
        this.topic = topic;
    }
}
