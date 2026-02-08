package whoreads.backend.domain.dna.entity;

import jakarta.persistence.*;
import lombok.*;
import whoreads.backend.domain.dna.enums.GenreCode;

@Builder
@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table
public class DnaOption {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String content;

//    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private GenreCode genre;

    @Column(nullable = false)
    private int score;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id")
    private DnaQuestion question;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "track_id")
    DnaTrack track;
}
