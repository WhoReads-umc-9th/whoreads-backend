package whoreads.backend.domain.readingsession.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import whoreads.backend.domain.member.entity.Member;
import whoreads.backend.domain.readingsession.enums.SessionStatus;
import whoreads.backend.global.entity.BaseEntity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
    private Integer totalMinutes;

    @Column(name = "finished_at")
    private LocalDateTime finishedAt;

    @OneToMany(mappedBy = "readingSession", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReadingInterval> intervals = new ArrayList<>();

    @Builder
    public ReadingSession(Member member) {
        this.member = member;
        this.status = SessionStatus.IN_PROGRESS;
        this.totalMinutes = 0;
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

    public void complete() {
        if (this.status == SessionStatus.COMPLETED) {
            throw new IllegalStateException("이미 완료된 세션입니다.");
        }
        this.status = SessionStatus.COMPLETED;
        this.totalMinutes = calculateTotalMinutes();
        this.finishedAt = LocalDateTime.now();
    }

    public void addInterval(ReadingInterval interval) {
        this.intervals.add(interval);
        interval.setReadingSession(this);
    }

    public void removeInterval(ReadingInterval interval) {
        this.intervals.remove(interval);
        interval.setReadingSession(null);
    }

    public int calculateTotalMinutes() {
        return this.intervals.stream()
                .filter(interval -> interval.getDurationMinutes() != null)
                .mapToInt(ReadingInterval::getDurationMinutes)
                .sum();
    }
}
