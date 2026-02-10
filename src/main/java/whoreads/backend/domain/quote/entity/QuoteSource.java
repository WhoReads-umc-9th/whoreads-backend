package whoreads.backend.domain.quote.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "quote_source")
public class QuoteSource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    // 인용 하나당 출처 하나 (1:1)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quote_id", unique = true, nullable = false)
    private Quote quote;

    @Column(columnDefinition = "TEXT")
    private String sourceUrl;

    @Enumerated(EnumType.STRING)
    private QuoteSourceType sourceType;

    private String timestamp;

    // 직접 인용 여부 (true: 직접, false: 간접 등)
    // 요청사항: 추천사 여부와 반대로 작업 (isDirectQuote=true -> 직접 말함)
    private boolean isDirectQuote;

    @Builder
    public QuoteSource(Quote quote, String sourceUrl, QuoteSourceType sourceType, String timestamp, boolean isDirectQuote) {
        this.quote = quote;
        this.sourceUrl = sourceUrl;
        this.sourceType = sourceType;
        this.timestamp = timestamp;
        this.isDirectQuote = isDirectQuote;
    }
}