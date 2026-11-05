package whoreads.backend.domain.readingsession.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import whoreads.backend.domain.member.entity.Member;
import whoreads.backend.domain.readingsession.enums.SessionStatus;
import whoreads.backend.global.entity.BaseEntity;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "reading_session")
public class ReadingSession extends BaseEntity {

    // 프론트가 주기를 보내지 않았을 때 적용하는 기본값 (분 단위)
    public static final long DEFAULT_HEARTBEAT_INTERVAL_MINUTES = 1L;
    public static final long DEFAULT_SUSPEND_AFTER_MINUTES = 5L;
    public static final long DEFAULT_COMPLETE_AFTER_MINUTES = 120L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SessionStatus status;

    @Column(name = "total_minutes")
    private Long totalMinutes;

    @Column(name = "finished_at")
    private LocalDateTime finishedAt;

    @Column(name = "last_heartbeat_at")
    private LocalDateTime lastHeartbeatAt;

    @Column(name = "remaining_minutes")
    private Long remainingMinutes;

    // 세션 시작 시 프론트에서 받는 타이머 주기 (분 단위). 보내지 않으면 아래 기본값을 쓴다
    @Column(name = "heartbeat_interval_minutes", nullable = false)
    private Long heartbeatIntervalMinutes;

    // 마지막 heartbeat로부터 이 시간이 지나면 IN_PROGRESS 세션을 SUSPENDED로 전환
    @Column(name = "suspend_after_minutes", nullable = false)
    private Long suspendAfterMinutes;

    // 일시정지 후(또는 마지막 heartbeat 후) 이 시간이 지나면 PAUSED 세션을 COMPLETED로 전환하고 재개를 막는다
    @Column(name = "complete_after_minutes", nullable = false)
    private Long completeAfterMinutes;

    @OneToMany(mappedBy = "readingSession", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReadingInterval> intervals = new ArrayList<>();

    @Builder
    public ReadingSession(Member member, Long heartbeatIntervalMinutes, Long suspendAfterMinutes, Long completeAfterMinutes) {
        this.member = member;
        this.status = SessionStatus.IN_PROGRESS;
        this.totalMinutes = 0L;
        this.lastHeartbeatAt = LocalDateTime.now(); // 세션 시작 시 초기화
        this.heartbeatIntervalMinutes = heartbeatIntervalMinutes != null ? heartbeatIntervalMinutes : DEFAULT_HEARTBEAT_INTERVAL_MINUTES;
        this.suspendAfterMinutes = suspendAfterMinutes != null ? suspendAfterMinutes : DEFAULT_SUSPEND_AFTER_MINUTES;
        this.completeAfterMinutes = completeAfterMinutes != null ? completeAfterMinutes : DEFAULT_COMPLETE_AFTER_MINUTES;

        // heartbeat 주기보다 짧게 잡으면 정상적으로 heartbeat를 보내는 중에도 세션이 중단된다
        if (this.suspendAfterMinutes < this.heartbeatIntervalMinutes) {
            throw new IllegalStateException("suspend_after_minutes는 heartbeat_interval_minutes보다 작을 수 없습니다.");
        }
        // 중단되기도 전에 이어하기 기한이 끝나면, 세션은 IN_PROGRESS로 남아 새 세션 시작을 막는데 recover는 만료로 거절된다
        if (this.completeAfterMinutes < this.suspendAfterMinutes) {
            throw new IllegalStateException("complete_after_minutes는 suspend_after_minutes보다 작을 수 없습니다.");
        }
    }

    public void pause() {
        if (this.status != SessionStatus.IN_PROGRESS) {
            throw new IllegalStateException("진행 중인 세션만 일시정지할 수 있습니다. 현재 상태: " + this.status);
        }
        this.status = SessionStatus.PAUSED;
    }

    public void resume() {
        if (this.status != SessionStatus.PAUSED) {
            throw new IllegalStateException("일시정지된 세션만 재개할 수 있습니다. 현재 상태: " + this.status);
        }
        this.status = SessionStatus.IN_PROGRESS;
    }

    public void recover() {
        if (this.status != SessionStatus.SUSPENDED) {
            throw new IllegalStateException("SUSPENDED 상태인 세션만 재개할 수 있습니다. 현재 상태: " + this.status);
        }
        this.status = SessionStatus.IN_PROGRESS;
    }

    public void updateHeartbeat(LocalDateTime heartbeatAt) {
        Objects.requireNonNull(heartbeatAt, "heartbeatAt must not be null");
        this.lastHeartbeatAt = heartbeatAt;
    }

    public void suspend(Long goalMinutes) {
        if (this.status != SessionStatus.IN_PROGRESS && this.status != SessionStatus.PAUSED) {
            throw new IllegalStateException("진행 중인 세션만 중단할 수 있습니다.");
        }

        // 아직 닫히지 않은 모든 인터벌을 마지막 하트비트 시점으로 닫아버림
        for (ReadingInterval interval : this.intervals) {
            if (interval.getEndTime() == null) {
                LocalDateTime endTime = this.lastHeartbeatAt != null ? this.lastHeartbeatAt : interval.getStartTime().plusSeconds(1);
                if (!endTime.isAfter(interval.getStartTime())) {
                    endTime = interval.getStartTime().plusSeconds(1);
                }
                interval.end(endTime);
            }
        }

        this.status = SessionStatus.SUSPENDED;
        this.finishedAt = this.lastHeartbeatAt;

        // 지금까지 읽은 총 시간 계산 (total_read_times)
        this.totalMinutes = calculateTotalMinutes();

        // 남은 시간 계산 (remaining_times)
        this.remainingMinutes = goalMinutes - this.totalMinutes;
    }

    public void complete() {
        if (this.status == SessionStatus.COMPLETED) {
            throw new IllegalStateException("이미 완료된 세션입니다.");
        }
        this.status = SessionStatus.COMPLETED;
        this.totalMinutes = calculateTotalMinutes();
        this.finishedAt = LocalDateTime.now();
    }

    // 일시정지·중단된 채 남은 세션을 마지막으로 읽은 시점 기준으로 완료 (새 세션 시작 시)
    // finishedAt은 실제로 읽기를 멈춘 시각으로 두어 기록이 실제 읽은 날짜에 집계되도록 한다
    // - SUSPENDED: suspend()에서 설정한 마지막 heartbeat 시각을 유지
    // - PAUSED: 마지막 구간의 종료 시각(일시정지한 시각). 중단됐다가 이어 읽은 세션은 옛 finishedAt이 남아 있으므로 그 값을 쓰지 않는다
    public void completeLeftover() {
        if (this.status != SessionStatus.PAUSED && this.status != SessionStatus.SUSPENDED) {
            throw new IllegalStateException("일시정지·중단된 세션만 자동 완료할 수 있습니다. 현재 상태: " + this.status);
        }
        boolean keepFinishedAt = this.status == SessionStatus.SUSPENDED && this.finishedAt != null;
        this.status = SessionStatus.COMPLETED;
        this.totalMinutes = calculateTotalMinutes();
        if (!keepFinishedAt) {
            LocalDateTime pausedAt = latestIntervalEndTime();
            this.finishedAt = pausedAt != null ? pausedAt : this.lastHeartbeatAt;
        }
    }

    private LocalDateTime latestIntervalEndTime() {
        return this.intervals.stream()
                .map(ReadingInterval::getEndTime)
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(null);
    }

    public void addInterval(ReadingInterval interval) {
        this.intervals.add(interval);
        interval.setReadingSession(this);
    }

    public void removeInterval(ReadingInterval interval) {
        this.intervals.remove(interval);
        interval.setReadingSession(null);
    }

    // 구간별 내림 오차가 누적되지 않도록 Duration 그대로 합산한 뒤 전체 합계만 분으로 반올림
    public long calculateTotalMinutes() {
        Duration total = this.intervals.stream()
                .filter(interval -> interval.getEndTime() != null)
                .map(interval -> Duration.between(interval.getStartTime(), interval.getEndTime()))
                .reduce(Duration.ZERO, Duration::plus);
        return Math.round(total.toMillis() / 60_000.0);
    }

    // 반올림한 총 독서 시간이 0분인 세션 (30초 미만). 기록·통계에 남기지 않는다
    public boolean hasNoReadTime() {
        return this.totalMinutes == null || this.totalMinutes == 0L;
    }

    public void processIdleTime(LocalDateTime now) {
        Objects.requireNonNull(now, "now must not be null");
        this.lastHeartbeatAt = now;
    }
}
