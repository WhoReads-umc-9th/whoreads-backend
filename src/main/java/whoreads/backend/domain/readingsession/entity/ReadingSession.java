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

    @OneToMany(mappedBy = "readingSession", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReadingInterval> intervals = new ArrayList<>();

    @Builder
    public ReadingSession(Member member) {
        this.member = member;
        this.status = SessionStatus.IN_PROGRESS;
        this.totalMinutes = 0L;
        this.lastHeartbeatAt = LocalDateTime.now(); // 세션 시작 시 초기화
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
            this.finishedAt = this.intervals.stream()
                    .map(ReadingInterval::getEndTime)
                    .filter(Objects::nonNull)
                    .max(LocalDateTime::compareTo)
                    .orElse(this.lastHeartbeatAt);
        }
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

    public void processIdleTime(LocalDateTime now) {
        Objects.requireNonNull(now, "now must not be null");
        this.lastHeartbeatAt = now;
    }
}
