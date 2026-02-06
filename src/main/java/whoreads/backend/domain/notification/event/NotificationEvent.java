package whoreads.backend.domain.notification.event;

import java.util.List;

public interface NotificationEvent {
    record FollowEvent(
            Long celebId,
            String celebName,
            Long bookId,
            String bookName,
            String authorName
    ) implements NotificationEvent {} // 인터페이스 구현!

    record RoutineEvent(
            List<String> tokens
    ) implements NotificationEvent {} // 인터페이스 구현!
}