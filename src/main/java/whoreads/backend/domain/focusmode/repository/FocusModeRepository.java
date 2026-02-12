package whoreads.backend.domain.focusmode.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import whoreads.backend.domain.focusmode.entity.FocusTimerSetting;

import java.util.Optional;

public interface FocusModeRepository extends JpaRepository<FocusTimerSetting, Long> {

    Optional<FocusTimerSetting> findByMemberId(Long memberId);
}
