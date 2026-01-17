package whoreads.backend.domain.readingsession.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import whoreads.backend.domain.readingsession.entity.ReadingSession;

public interface ReadingSessionRepository extends JpaRepository<ReadingSession, Long> {
}
