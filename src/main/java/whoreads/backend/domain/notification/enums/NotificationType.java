package whoreads.backend.domain.notification.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import whoreads.backend.domain.notification.event.NotificationEvent;

@Getter
@RequiredArgsConstructor
public enum NotificationType {
    // DB에는 "FOLLOW", "ROUTINE"으로 저장됨
    FOLLOW(NotificationMessageType.NEW_FOLLOW_BOOK),
    ROUTINE(NotificationMessageType.BOOK_ROUTINE);

    // 메시지 생성 로직을 담은 클래스와 연결
    private final NotificationMessageType messageType;

    public String[] generateMessage(NotificationEvent event) {
        return this.messageType.generate(event);
    }
}