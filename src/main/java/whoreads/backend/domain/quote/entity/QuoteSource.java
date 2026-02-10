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
    @Column(name = "source_type", nullable = false)
    private QuoteSourceType sourceType;

    private String timestamp;

    @Builder
    public QuoteSource(Quote quote, String sourceUrl, QuoteSourceType sourceType, String timestamp) {
        this.quote = quote;
        this.sourceUrl = sourceUrl;
        this.sourceType = sourceType;
        this.timestamp = timestamp;
    }
}