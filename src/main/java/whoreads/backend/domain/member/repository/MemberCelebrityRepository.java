package whoreads.backend.domain.member.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import whoreads.backend.domain.celebrity.entity.Celebrity;
import whoreads.backend.domain.member.entity.Member;
import whoreads.backend.domain.member.entity.MemberCelebrity;

import java.util.List;

public interface MemberCelebrityRepository extends JpaRepository<MemberCelebrity, Long> {

    @Query("SELECT mc.celebrity FROM MemberCelebrity mc " +
            "JOIN mc.celebrity " +
            "WHERE mc.member.id = :memberId")
    List<Celebrity> findCelebritiesByMemberId(@Param("memberId") Long memberId);

    // 팔로우 중인지 확인
    boolean existsByMemberAndCelebrity(Member member, Celebrity celebrity);

    // 언팔로우 기능을 위해 관계 삭제 메서드
    void deleteByMemberAndCelebrity(Member member, Celebrity celebrity);
}
