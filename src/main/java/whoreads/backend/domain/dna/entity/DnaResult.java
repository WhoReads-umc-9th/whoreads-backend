package whoreads.backend.domain.dna.entity;

import jakarta.persistence.*;
import lombok.*;
import whoreads.backend.domain.celebrity.entity.Celebrity;
import whoreads.backend.domain.member.entity.Member;
import whoreads.backend.global.entity.BaseEntity;

@Builder
@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table
public class DnaResult extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "celebrity_id")
    private Celebrity celebrity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "track_id")
    private DnaTrack track;

}
