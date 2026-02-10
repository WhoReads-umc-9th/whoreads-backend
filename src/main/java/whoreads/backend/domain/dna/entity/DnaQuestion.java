package whoreads.backend.domain.dna.entity;

import jakarta.persistence.*;
import lombok.*;

@Builder
@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table
public class DnaQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "track_id")
    private DnaTrack track;

    @Column(nullable = false)
    private int step;

    @Column(nullable = false)
    private String content;
}
