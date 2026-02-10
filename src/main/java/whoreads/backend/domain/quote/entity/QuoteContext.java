package whoreads.backend.domain.quote.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "quote_context")
public class QuoteContext {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    // 인용 하나당 맥락 하나 (1:1)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quote_id", unique = true, nullable = false)
    private Quote quote;

    @Column(columnDefinition = "TEXT")
    private String contextHow;  // 계기

    @Column(columnDefinition = "TEXT")
    private String contextWhen; // 시기

    @Column(columnDefinition = "TEXT")
    private String contextWhy;  // 이유

    @Column(columnDefinition = "TEXT")
    private String contextHelp; // 도움

    @Builder
    public QuoteContext(Quote quote, String contextHow, String contextWhen, String contextWhy, String contextHelp) {
        this.quote = quote;
        this.contextHow = contextHow;
        this.contextWhen = contextWhen;
        this.contextWhy = contextWhy;
        this.contextHelp = contextHelp;
    }
}