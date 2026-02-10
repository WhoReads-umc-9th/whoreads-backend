package whoreads.backend.domain.celebrity.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import whoreads.backend.domain.celebrity.entity.Celebrity;
import whoreads.backend.domain.celebrity.entity.CelebrityTag;

import java.util.List;
import java.util.Optional;

public interface CelebrityRepository extends JpaRepository<Celebrity, Long> {

    // 1. 전체 조회 (N+1 해결)
    @Override
    @EntityGraph(attributePaths = "jobTags")
    List<Celebrity> findAll();

    // 2. 태그 필터링 조회 (N+1 해결)
    @EntityGraph(attributePaths = "jobTags")
    List<Celebrity> findAllByJobTagsContains(CelebrityTag tag);

    // 3. 단건 조회 (상세 보기)
    @Override
    @EntityGraph(attributePaths = "jobTags")
    Optional<Celebrity> findById(Long id);
}