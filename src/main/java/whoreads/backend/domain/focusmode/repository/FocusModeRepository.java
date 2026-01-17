package whoreads.backend.domain.focusmode.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import whoreads.backend.domain.focusmode.entity.FocusMode;

public interface FocusModeRepository extends JpaRepository<FocusMode, Long> {
}
