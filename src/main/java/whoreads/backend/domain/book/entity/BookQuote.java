package whoreads.backend.domain.book.entity;

import jakarta.persistence.*;
import lombok.*;
import whoreads.backend.domain.quote.entity.Quote;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "book_quote")
public class BookQuote { // 책 - 인용 교차 테이블

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id")
    private Book book;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quote_id")
    private Quote quote;

    @Builder
    public BookQuote(Book book, Quote quote) {
        this.book = book;
        this.quote = quote;
    }
}