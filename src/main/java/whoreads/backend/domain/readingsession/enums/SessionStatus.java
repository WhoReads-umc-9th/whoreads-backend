package whoreads.backend.domain.readingsession.enums;

public enum SessionStatus {
    IN_PROGRESS,
    PAUSED,   // 사용자가 의도적으로 일시정지한 상태
    COMPLETED,
    SUSPENDED   // heartbeat 5분 이상 없으면 자동 전환되는 상태 (마지막 heartbeat 후 2시간까지 이어하기 가능)
}
