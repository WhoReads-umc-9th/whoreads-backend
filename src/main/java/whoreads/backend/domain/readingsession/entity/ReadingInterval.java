package whoreads.backend.domain.readingsession.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "reading_interval")
public class ReadingInterval {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private ReadingSession readingSession;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "duration_minutes")
    private Long durationMinutes;

    @Builder
    public ReadingInterval(ReadingSession readingSession, LocalDateTime startTime) {
        this.readingSession = readingSession;
        this.startTime = startTime;
    }

    public void end(LocalDateTime endTime) {
        if (this.endTime != null) {
            throw new IllegalStateException("이미 종료된 인터벌입니다.");
        }
        if (endTime.isBefore(this.startTime)) {
            throw new IllegalArgumentException("종료 시간은 시작 시간 이후여야 합니다.");
        }
        this.durationMinutes = ChronoUnit.MINUTES.between(this.startTime, endTime);
        this.endTime = endTime;
    }

    // 양방향 관계 설정용 (패키지 전용)
    void setReadingSession(ReadingSession readingSession) {
        this.readingSession = readingSession;
    }
}
