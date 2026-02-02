package whoreads.backend.domain.notification.service;

public interface NotificationTokenService {
    void updateToken(Long userId, String token);
    void deleteToken(Long userId);
    String getToken(Long userId);
}
