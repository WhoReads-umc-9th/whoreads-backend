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

    // 새로 추가한 필드 - 현
    @Column(name = "timer_minutes", nullable = false)
    private Long timerMinutes = 0L;

    @Builder
    public FocusTimerSetting(Member member, Long timerMinutes) {
        this.member = member;
        this.focusBlockEnabled = false;
        this.whiteNoiseEnabled = false;
        this.timerMinutes = (timerMinutes != null) ? timerMinutes : 0L;
    }
    public void updateFocusBlockEnabled(Boolean enabled) {
        this.focusBlockEnabled = Boolean.TRUE.equals(enabled);
    }

    public void updateWhiteNoiseEnabled(Boolean enabled) {
        this.whiteNoiseEnabled = Boolean.TRUE.equals(enabled);
    }

    public void updateTimerMinutes(Long minutes) { // 추가 - 현
        this.timerMinutes = minutes;
    }
}
