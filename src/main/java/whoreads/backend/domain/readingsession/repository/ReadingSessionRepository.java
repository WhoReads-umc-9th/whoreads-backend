package whoreads.backend.domain.readingsession.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import whoreads.backend.domain.readingsession.entity.ReadingSession;
import whoreads.backend.domain.readingsession.enums.SessionStatus;

public interface ReadingSessionRepository extends JpaRepository<ReadingSession, Long> {

    @Query("SELECT COALESCE(SUM(rs.totalMinutes), 0) FROM ReadingSession rs " +
            "WHERE rs.member.id = :memberId AND rs.status = :status")
    Long sumTotalMinutesByMemberIdAndStatus(@Param("memberId") Long memberId, @Param("status") SessionStatus status);
}
