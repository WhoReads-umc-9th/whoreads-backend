package whoreads.backend.domain.focusmode.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import whoreads.backend.domain.focusmode.entity.BlockedApp;

import java.util.List;

public interface BlockedAppRepository extends JpaRepository<BlockedApp, Long> {

    List<BlockedApp> findByMemberId(Long memberId);

    void deleteByMemberId(Long memberId);
}
