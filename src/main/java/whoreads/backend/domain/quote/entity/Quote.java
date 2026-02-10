package whoreads.backend.domain.quote.entity;

import jakarta.persistence.*;
import lombok.*;
import whoreads.backend.domain.celebrity.entity.Celebrity;
import whoreads.backend.global.entity.BaseEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "quote")
public class Quote extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "quote_id")
    private Long id;

    // 1. 내용 (필수, TEXT 타입)
    @Column(columnDefinition = "TEXT", nullable = false)
    private String originalText;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Language language; // KO, EN

    // 2. 점수 (필수)
    @Column(nullable = false)
    private int contextScore;

    // 3. 인물 연결 (필수)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "celebrity_id", nullable = false)
    private Celebrity celebrity;

    @Builder
    public Quote(String originalText, Language language, int contextScore, Celebrity celebrity) {
        this.originalText = originalText;
        this.language = language;
        this.contextScore = contextScore;
        this.celebrity = celebrity;
    }

    public enum Language {
        KO, EN
    }
}