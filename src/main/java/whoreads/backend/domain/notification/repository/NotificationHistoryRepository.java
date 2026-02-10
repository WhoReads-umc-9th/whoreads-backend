package whoreads.backend.domain.notification.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import whoreads.backend.domain.notification.entity.NotificationHistory;

import java.time.LocalDateTime;
import java.util.List;


public interface NotificationHistoryRepository extends JpaRepository<NotificationHistory, Long> {
    @Query("SELECT n FROM NotificationHistory n " +
            "WHERE n.member.id = :memberId " +
            "AND (:cursor IS NULL OR n.id < :cursor) " +
            "ORDER BY n.id DESC")
    List<NotificationHistory> findNotificationHistoriesWithCursor(
            Long memberId,
            Long cursor,
            Pageable pageable);

    void deleteByCreatedAtBefore(LocalDateTime cutoff);
}