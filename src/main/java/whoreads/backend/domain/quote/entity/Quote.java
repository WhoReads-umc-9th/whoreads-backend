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

    // 1. 내용 및 언어
    @Column(columnDefinition = "TEXT", nullable = false)
    private String originalText;

    @Enumerated(EnumType.STRING)
    private Language language; // KO, EN

    // 2. 점수
    private int contextScore;

    // 3. 인물 연결
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "celebrity_id")
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