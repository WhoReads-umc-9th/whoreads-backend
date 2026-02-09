package whoreads.backend.domain.notification.service;


public interface NotificationPushService {

    void sendNotification(String title, String body, String fcmToken,String link);
}
