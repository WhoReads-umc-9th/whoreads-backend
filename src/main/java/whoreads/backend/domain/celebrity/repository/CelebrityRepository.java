package whoreads.backend.domain.celebrity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import whoreads.backend.domain.celebrity.entity.Celebrity;

public interface CelebrityRepository extends JpaRepository<Celebrity, Long> {
}
