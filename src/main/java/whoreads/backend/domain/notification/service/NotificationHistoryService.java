package whoreads.backend.domain.notification.service;

import whoreads.backend.domain.notification.dto.FcmMessageDTO;
import whoreads.backend.domain.notification.dto.NotificationResDTO;

public interface NotificationHistoryService {
   NotificationResDTO.TotalInboxDTO getNotificationHistory(Long memberId, Long cursor, int size);
   void readNotification(Long memberId, Long notificationId);
   void saveHistory(Long memberId, FcmMessageDTO dto);
}
