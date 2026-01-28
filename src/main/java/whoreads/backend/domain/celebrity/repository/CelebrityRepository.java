package whoreads.backend.domain.celebrity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import whoreads.backend.domain.celebrity.entity.Celebrity;
import whoreads.backend.domain.celebrity.entity.CelebrityTag;

import java.util.List;

public interface CelebrityRepository extends JpaRepository<Celebrity, Long> {

    // 1. 특정 직업 태그를 포함하는 유명인 찾기
    List<Celebrity> findAllByJobTagsContains(CelebrityTag tag);

    // 2. 전체 조회는 기본 findAll() 사용 ㄱㄱ
}