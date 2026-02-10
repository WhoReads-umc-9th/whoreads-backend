package whoreads.backend.domain.focusmode.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import whoreads.backend.domain.member.entity.Member;
import whoreads.backend.global.entity.BaseEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "blocked_app")
public class BlockedApp extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "bundle_id", nullable = false)
    private String bundleId;

    @Column(nullable = false, length = 100)
    private String name;

    @Builder
    public BlockedApp(Member member, String bundleId, String name) {
        this.member = member;
        this.bundleId = bundleId;
        this.name = name;
    }
}
