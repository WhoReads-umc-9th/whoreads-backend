package whoreads.backend.domain.notification.service;

import org.springframework.stereotype.Service;

@Service
public class NotificationTokenServiceImpl implements NotificationTokenService {
    public void updateToken(Long userId,String token){}
    public void deleteToken(Long userId){}
    public String getToken(Long userId){return null;}
}
