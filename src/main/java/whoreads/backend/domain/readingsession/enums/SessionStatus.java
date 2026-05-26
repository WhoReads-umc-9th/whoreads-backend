package whoreads.backend.domain.readingsession.enums;

public enum SessionStatus {
    IN_PROGRESS,
    PAUSED,   // 사용자가 의도적으로 일시정지한 상태
    COMPLETED,
    SUSPENDED   // heartbeat 2시간 초과시 자동 전환되는 상태
}
