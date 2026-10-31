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

    // 바꾼 이유: 요청 DTO(SourceInfo.isDirect)와 DB 컬럼(is_direct_quote)은 있는데
    // 엔티티에만 없어서 API로 받은 값이 저장되지 않고 버려지고 있었음
    @Column(name = "is_direct_quote", nullable = false)
    private boolean directQuote;

    @Builder
    public QuoteSource(Quote quote, String sourceUrl, QuoteSourceType sourceType, String timestamp,
                       boolean directQuote) {
        this.quote = quote;
        this.sourceUrl = sourceUrl;
        this.sourceType = sourceType;
        this.timestamp = timestamp;
        this.directQuote = directQuote;
    }
}