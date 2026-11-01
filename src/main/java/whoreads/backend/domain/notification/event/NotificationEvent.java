package whoreads.backend.domain.notification.event;

import whoreads.backend.domain.notification.dto.MemberTokenDTO;

import java.util.List;

import java.time.LocalDateTime;

public interface NotificationEvent {

    record FollowEvent(
            Long celebId,
            String celebName,
            Long bookId,
            String bookName,
            String authorName,
            String idempotencyKey
    ) implements NotificationEvent {}

    record RoutineEvent(
            List<MemberTokenDTO> memberTokens,
            String idempotencyKey
    ) implements NotificationEvent {}

    record TimerCompletedEvent(
            Long memberId,
            Long completedMinutes,
            LocalDateTime completedAt,
            String idempotencyKey
    ) implements NotificationEvent {}
}