package whoreads.backend.domain.notification.service;

import whoreads.backend.domain.notification.dto.NotificationReqDTO;
import whoreads.backend.domain.notification.dto.NotificationResDTO;

public interface NotificationService {
    NotificationResDTO.TotalSettingDTO getNotifications(Long userId,String type);
    NotificationResDTO.SettingDTO createNotification(Long userId, NotificationReqDTO.SettingDTO createSettingDTO);
    NotificationResDTO.SettingDTO updateNotification(Long userId,Long notificationId,NotificationReqDTO.SettingDTO updateSettingDTO);
    void deleteNotification(Long userId,Long notificationId);
}
