package whoreads.backend.domain.dna.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import whoreads.backend.domain.dna.entity.DnaResult;

import java.util.Optional;

public interface DnaResultRepository extends JpaRepository<DnaResult, Long> {

    // 특정 회원의 가장 마지막 테스트 결과 가져오기
    Optional<DnaResult> findTopByMemberIdOrderByCreatedAtDesc(Long memberId);
}
