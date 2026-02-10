package whoreads.backend.domain.dna.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import whoreads.backend.domain.dna.entity.DnaQuestion;
import whoreads.backend.domain.dna.enums.TrackCode;

import java.util.List;
import java.util.Optional;

public interface DnaQuestionRepository extends JpaRepository<DnaQuestion, Long> {

    // Q1 조회를 위해 ste 번호로 질문 찾기
    Optional<DnaQuestion> findByStep(int step);

    // Q1 응답 후 특정 트랙에 속한 Q2~Q5 질문들을 순서대로 가져오기
    List<DnaQuestion> findByTrackTrackCodeAndStepBetweenOrderByStepAsc(TrackCode trackCode, int step1, int step2);
}
