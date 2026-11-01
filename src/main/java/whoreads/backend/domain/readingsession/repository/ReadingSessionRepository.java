package whoreads.backend.domain.readingsession.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import whoreads.backend.domain.readingsession.entity.ReadingSession;
import whoreads.backend.domain.readingsession.enums.SessionStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReadingSessionRepository extends JpaRepository<ReadingSession, Long> {

    @Query("SELECT COALESCE(SUM(rs.totalMinutes), 0) FROM ReadingSession rs " +
            "WHERE rs.member.id = :memberId AND rs.status = :status")
    Long sumTotalMinutesByMemberIdAndStatus(@Param("memberId") Long memberId, @Param("status") SessionStatus status);

//    Optional<ReadingSession> findByMemberIdAndStatusIn(Long memberId, List<SessionStatus> statuses);
    @EntityGraph(attributePaths = {"intervals"})
    List<ReadingSession> findAllByMemberIdAndStatusIn(Long memberId, List<SessionStatus> statuses);

    @EntityGraph(attributePaths = {"intervals"})
    Optional<ReadingSession> findFirstByMemberIdAndStatusInOrderByCreatedAtDesc(Long memberId, List<SessionStatus> statuses);

    @Query("SELECT COALESCE(SUM(rs.totalMinutes), 0) FROM ReadingSession rs " +
            "WHERE rs.member.id = :memberId AND rs.status = 'COMPLETED' " +
            "AND rs.finishedAt >= :start AND rs.finishedAt < :end")
    Long sumTotalMinutesByMemberIdAndFinishedAtBetween(
            @Param("memberId") Long memberId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    @Query("SELECT rs FROM ReadingSession rs " +
            "WHERE rs.member.id = :memberId AND rs.status = 'COMPLETED' " +
            "AND rs.finishedAt >= :start AND rs.finishedAt < :end " +
            "ORDER BY rs.createdAt ASC")
    List<ReadingSession> findCompletedByMemberIdAndFinishedAtBetween(
            @Param("memberId") Long memberId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    // 일시정지한 시각(마지막 구간의 종료 시각)이 threshold보다 오래된 일시정지 세션
    @Query("SELECT rs FROM ReadingSession rs JOIN FETCH rs.intervals " +
            "WHERE rs.status = 'PAUSED' AND (SELECT MAX(i.endTime) FROM ReadingInterval i WHERE i.readingSession = rs) < :threshold")
    List<ReadingSession> findPausedSessionsPausedBefore(@Param("threshold") LocalDateTime threshold);

    @Query("SELECT rs FROM ReadingSession rs JOIN FETCH rs.intervals " +
            "WHERE rs.status = 'IN_PROGRESS' AND rs.lastHeartbeatAt < :threshold")
    List<ReadingSession> findStaleInProgressSessions(@Param("threshold") LocalDateTime threshold);
}
