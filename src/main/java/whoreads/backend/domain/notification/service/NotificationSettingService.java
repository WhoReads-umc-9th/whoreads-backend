package whoreads.backend.domain.notification.service;

import whoreads.backend.domain.notification.dto.NotificationReqDTO;
import whoreads.backend.domain.notification.dto.NotificationResDTO;

public interface NotificationSettingService {
    NotificationResDTO.TotalSettingDTO getNotificationSettings(Long userId,String type);
    NotificationResDTO.SettingDTO createNotificationSetting(Long userId, NotificationReqDTO.SettingDTO createSettingDTO);
    NotificationResDTO.SettingDTO updateNotificationSetting(Long userId,Long notificationId,NotificationReqDTO.SettingDTO updateSettingDTO);
    void deleteNotificationSetting(Long userId,Long notificationId);
}
