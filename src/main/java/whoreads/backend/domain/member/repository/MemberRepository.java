package whoreads.backend.domain.member.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import whoreads.backend.domain.member.entity.Member;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByLoginId(String loginId);

    boolean existsByEmail(String email);

    boolean existsByLoginId(String loginId);
}
