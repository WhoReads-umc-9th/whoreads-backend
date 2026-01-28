package whoreads.backend.domain.quote.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import whoreads.backend.domain.quote.entity.QuoteSource;
import java.util.Optional;

public interface QuoteSourceRepository extends JpaRepository<QuoteSource, Long> {
    // Quote ID로 출처 찾기
    Optional<QuoteSource> findByQuoteId(Long quoteId);
}