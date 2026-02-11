package whoreads.backend.domain.celebrity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import whoreads.backend.domain.celebrity.entity.CelebrityBook;

public interface CelebrityBookRepository extends JpaRepository<CelebrityBook, Long> {
}
