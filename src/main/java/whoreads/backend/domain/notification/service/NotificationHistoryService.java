package whoreads.backend.domain.notification.service;

import org.springframework.stereotype.Service;
import whoreads.backend.domain.notification.dto.NotificationResDTO;

@Service
public interface NotificationHistoryService {
   NotificationResDTO.TotalInboxDTO getNotificationHistory(Long memberId, Long cursor, int size);
   void readNotification(Long memberId, Long notificationId);
}
