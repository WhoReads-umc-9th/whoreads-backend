package whoreads.backend.domain.dna.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import whoreads.backend.domain.dna.entity.DnaQuestion;
import whoreads.backend.domain.dna.enums.TrackCode;

import java.util.List;
import java.util.Optional;

public interface DnaQuestionRepository extends JpaRepository<DnaQuestion, Long> {

    // Q1 조회를 위해 ste 번호로 질문 찾기
    Optional<DnaQuestion> findByStep(int step);

    // Q1 응답 후 특정 트랙에 속한 Q2~Q5 질문들을 순서대로 가져오기 (JPQL 적용)
    @Query("SELECT q FROM DnaQuestion q " +
            "WHERE q.track.trackCode = :trackCode " +
            "AND q.step BETWEEN :startStep AND :endStep " +
            "ORDER BY q.step ASC")
    List<DnaQuestion> findQuestionsByTrackAndStepRange(
            @Param("trackCode") TrackCode trackCode,
            @Param("startStep") int startStep,
            @Param("endStep") int endStep
    );
}
