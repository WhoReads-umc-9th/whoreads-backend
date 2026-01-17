package whoreads.backend.domain.dna.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import whoreads.backend.domain.dna.entity.DnaType;

public interface DnaTypeRepository extends JpaRepository<DnaType, Long> {
}
