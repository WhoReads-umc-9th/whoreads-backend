package whoreads.backend.domain.notification.service;

import whoreads.backend.domain.notification.dto.FcmMessageDTO;
import whoreads.backend.domain.notification.dto.NotificationResDTO;

public interface NotificationHistoryService {
   NotificationResDTO.TotalInboxDTO getNotificationHistory(Long memberId, Long cursor, int size);
   NotificationResDTO.HistoryDTO readNotification(Long memberId, Long notificationId);
   Void readAllNotifications(Long memberId);
   void saveHistory(Long memberId, FcmMessageDTO dto);
   void deleteNotification(Long memberId,Long notificationId);
}
