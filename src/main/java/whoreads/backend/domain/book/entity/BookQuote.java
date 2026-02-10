package whoreads.backend.domain.book.entity;

import jakarta.persistence.*;
import lombok.*;
import whoreads.backend.domain.quote.entity.Quote;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
// 복합 유니크 제약조건 (같은 책에 같은 인용 중복 저장 방지)
@Table(name = "book_quote", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"book_id", "quote_id"})
})
public class BookQuote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quote_id", nullable = false)
    private Quote quote;

    @Builder
    public BookQuote(Book book, Quote quote) {
        this.book = book;
        this.quote = quote;
    }
}