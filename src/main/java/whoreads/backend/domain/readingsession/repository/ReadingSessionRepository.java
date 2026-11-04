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

    // 마지막 heartbeat로부터 세션별 suspend_after_minutes가 지난 진행 중 세션
    @Query("SELECT rs FROM ReadingSession rs JOIN FETCH rs.intervals " +
            "WHERE rs.status = 'IN_PROGRESS' AND rs.lastHeartbeatAt + rs.suspendAfterMinutes minute < :now")
    List<ReadingSession> findStaleInProgressSessions(@Param("now") LocalDateTime now);

    // 일시정지한 시각(마지막 구간의 종료 시각)으로부터 세션별 complete_after_minutes가 지난 일시정지 세션
    @Query("SELECT rs FROM ReadingSession rs JOIN FETCH rs.intervals " +
            "WHERE rs.status = 'PAUSED' " +
            "AND (SELECT MAX(i.endTime) FROM ReadingInterval i WHERE i.readingSession = rs) + rs.completeAfterMinutes minute < :now")
    List<ReadingSession> findPausedSessionsPausedBefore(@Param("now") LocalDateTime now);
}
