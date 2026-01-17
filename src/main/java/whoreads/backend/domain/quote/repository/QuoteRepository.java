package whoreads.backend.domain.quote.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import whoreads.backend.domain.quote.entity.Quote;

public interface QuoteRepository extends JpaRepository<Quote, Long> {
}
