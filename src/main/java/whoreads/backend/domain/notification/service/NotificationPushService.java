package whoreads.backend.domain.notification.service;


import whoreads.backend.domain.notification.dto.FcmMessageDTO;

import java.util.List;

public interface NotificationPushService {

   void sendNotification(String fcmToken, FcmMessageDTO dto);
   void sendMulticast(List<String> tokens, FcmMessageDTO dto);
}
