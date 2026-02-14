package whoreads.backend.domain.member.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import whoreads.backend.domain.celebrity.entity.Celebrity;
import whoreads.backend.domain.member.entity.MemberCelebrity;

import java.util.List;

public interface MemberCelebrityRepository extends JpaRepository<MemberCelebrity, Long> {

    @Query("SELECT mc.celebrity FROM MemberCelebrity mc " +
            "JOIN mc.celebrity " +
            "WHERE mc.member.id = :memberId")
    List<Celebrity> findCelebritiesByMemberId(@Param("memberId") Long memberId);
}
