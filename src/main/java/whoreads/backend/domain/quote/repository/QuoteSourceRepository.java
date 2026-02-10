package whoreads.backend.domain.quote.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import whoreads.backend.domain.quote.entity.QuoteSource;
import java.util.List;
import java.util.Optional;

public interface QuoteSourceRepository extends JpaRepository<QuoteSource, Long> {
    // Quote ID로 출처 찾기
    Optional<QuoteSource> findByQuoteId(Long quoteId);

    // 여러 Quote ID에 대한 출처 배치 조회 (N+1 방지)
    List<QuoteSource> findByQuoteIdIn(List<Long> quoteIds);
}