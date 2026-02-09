package whoreads.backend.domain.dna.entity;

import jakarta.persistence.*;
import lombok.*;
import whoreads.backend.domain.dna.enums.TrackCode;

@Builder
@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table
public class DnaTrack {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = false)
    TrackCode trackCode;

    @Column(nullable = false)
    private String name;
}
