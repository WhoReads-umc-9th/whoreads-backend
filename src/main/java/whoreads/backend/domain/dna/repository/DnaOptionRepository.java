package whoreads.backend.domain.dna.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import whoreads.backend.domain.dna.entity.DnaOption;
import whoreads.backend.domain.dna.entity.DnaQuestion;

import java.util.List;

public interface DnaOptionRepository extends JpaRepository<DnaOption, Long> {

    // 사용자가 선택한 Q2~Q5의 보기 ID 리스트로 모든 정보를 한꺼번에 조회
//    List<DnaOption> findAllById(List<Long> ids);

    List<DnaOption> findByQuestion(DnaQuestion rootQuestion);
}
