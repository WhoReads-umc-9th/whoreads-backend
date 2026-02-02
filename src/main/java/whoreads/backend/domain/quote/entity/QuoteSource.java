package whoreads.backend.domain.quote.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "quote_source")
public class QuoteSource { // 출처 및 증거

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quote_id")
    private Quote quote;

    private String sourceUrl;

    @Enumerated(EnumType.STRING)
    private QuoteSourceType sourceType;

    // 타임스탬프 (영상일 경우)
    private String timestamp;

    // 직접 인용 여부
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