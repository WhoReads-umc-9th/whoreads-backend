package whoreads.backend.domain.member.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import whoreads.backend.domain.member.entity.Member;

import java.time.LocalDateTime;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByLoginId(String loginId);

    boolean existsByEmail(String email);

    boolean existsByLoginId(String loginId);

    @Modifying(clearAutomatically = true)
    @Query("""
        UPDATE Member m
        SET m.fcmToken = null,
            m.fcmTokenUpdatedAt = null
        WHERE m.fcmToken = :token
    """)
    void clearToken(String token);

    @Modifying(clearAutomatically = true)
    @Query("""
          UPDATE Member m
          SET m.fcmToken = null, m.fcmTokenUpdatedAt = null
          WHERE m.fcmTokenUpdatedAt < :threshold""")
    void clearExpiredTokens(LocalDateTime threshold);
}
