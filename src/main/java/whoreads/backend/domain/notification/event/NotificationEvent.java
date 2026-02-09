package whoreads.backend.domain.notification.event;

import whoreads.backend.domain.notification.dto.MemberTokenDTO;

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
            List<MemberTokenDTO> memberTokens
    ) implements NotificationEvent {} // 인터페이스 구현!
}