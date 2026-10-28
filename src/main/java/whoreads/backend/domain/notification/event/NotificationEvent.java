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
    ) implements NotificationEvent {}

    record RoutineEvent(
            List<MemberTokenDTO> memberTokens
    ) implements NotificationEvent {}

    record TimerCompletedEvent(
            Long memberId,
            Long completedMinutes
    ) implements NotificationEvent {}
}