package whoreads.backend.domain.quote.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "quote_context")
public class QuoteContext { // 독서맥락

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quote_id")
    private Quote quote;

    private String contextHow;  // 읽게 된 계기
    private String contextWhen; // 어떤 시기에 읽었는지
    private String contextWhy;  // 왜 이 책이었나
    private String contextHelp; // 어떤 도움을 받았나

    @Builder
    public QuoteContext(Quote quote, String contextHow, String contextWhen, String contextWhy, String contextHelp) {
        this.quote = quote;
        this.contextHow = contextHow;
        this.contextWhen = contextWhen;
        this.contextWhy = contextWhy;
        this.contextHelp = contextHelp;
    }
}