package whoreads.backend.domain.quote.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import whoreads.backend.domain.quote.entity.QuoteContext;
import java.util.Optional;

public interface QuoteContextRepository extends JpaRepository<QuoteContext, Long> {
    // Quote ID로 맥락 찾기
    Optional<QuoteContext> findByQuoteId(Long quoteId);
}