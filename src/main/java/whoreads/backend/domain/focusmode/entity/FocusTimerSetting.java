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
@Table(name = "focus_timer_setting")
public class FocusTimerSetting extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false, unique = true)
    private Member member;

    @Column(name = "focus_block_enabled", nullable = false)
    private Boolean focusBlockEnabled = false;

    @Column(name = "white_noise_enabled", nullable = false)
    private Boolean whiteNoiseEnabled = false;

    @Builder
    public FocusTimerSetting(Member member) {
        this.member = member;
        this.focusBlockEnabled = false;
        this.whiteNoiseEnabled = false;
    }

    public void updateFocusBlockEnabled(Boolean enabled) {
        this.focusBlockEnabled = Boolean.TRUE.equals(enabled);
    }

    public void updateWhiteNoiseEnabled(Boolean enabled) {
        this.whiteNoiseEnabled = Boolean.TRUE.equals(enabled);
    }
}
