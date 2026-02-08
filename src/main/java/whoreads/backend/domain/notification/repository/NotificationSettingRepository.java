package whoreads.backend.domain.notification.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import whoreads.backend.domain.notification.entity.NotificationSetting;
import whoreads.backend.domain.notification.enums.NotificationType;

import java.util.List;

public interface NotificationSettingRepository extends JpaRepository<NotificationSetting, Long> {
    @Query("SELECT n FROM NotificationSetting n WHERE n.member.id = :userId " +
            "AND (:type IS NULL OR n.type = :type)")
    List<NotificationSetting> findAllByUserIdAndOptionalType(
            Long userId,
            NotificationType type
    );
    @Query(value = "SELECT DISTINCT m.fcm_token " +
            "FROM notification n " +
            "JOIN member m ON n.member_id = m.id " +
            "WHERE JSON_CONTAINS(n.days, JSON_QUOTE(:day)) " +
            "AND TIME_FORMAT(n.time, '%H:%i') = :time " +
            "AND n.type = 'ROUTINE' " +
            "AND n.is_enabled = 1 " +
            "AND m.fcm_token IS NOT NULL",
            nativeQuery = true) // 토큰이 있는 유저만
    List<String> findAllTokensByDayAndTime(String day, String time);
}
