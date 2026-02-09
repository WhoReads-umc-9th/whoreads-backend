package whoreads.backend.domain.notification.service;

import com.google.firebase.messaging.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import whoreads.backend.global.exception.CustomException;
import whoreads.backend.global.exception.ErrorCode;


@Service
@RequiredArgsConstructor
public class NotificationPushServiceImpl implements NotificationPushService {

    private final FirebaseMessaging firebaseMessaging;

    public void sendNotification(String title, String body, String fcmToken,String link) {
        send(createMessage(title, body, fcmToken,link));
    }

    private void send(Message message) {
        try {
            firebaseMessaging.send(message);
        } catch (FirebaseMessagingException e) {
            throw new CustomException(ErrorCode.FCM_SEND_FAILED);
        }
    }

    private Message createMessage(String title, String body, String fcmToken,String link) {
        return Message.builder()
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build())
                .setWebpushConfig(WebpushConfig.builder()
                        .setFcmOptions(WebpushFcmOptions.withLink(link))
                        .build())
                .putData("title", title)
                .putData("body", body)
                .setToken(fcmToken)
                .build();
    }
}
