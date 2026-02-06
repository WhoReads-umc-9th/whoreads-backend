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
    private Integer durationMinutes;

    `@Builder`
    public ReadingInterval(ReadingSession readingSession, LocalDateTime startTime) {
        this.readingSession = readingSession;
        this.startTime = startTime;
        readingSession.addInterval(this);
    }

    public void end(LocalDateTime endTime) {
        this.endTime = endTime;
        this.durationMinutes = (int) ChronoUnit.MINUTES.between(this.startTime, endTime);
    }
}
