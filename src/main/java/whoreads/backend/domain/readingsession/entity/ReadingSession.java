package whoreads.backend.domain.readingsession.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import whoreads.backend.global.entity.BaseEntity;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "reading_session")
public class ReadingSession extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // startTime은 BaseEntity의 createdAt 사용

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "focus_minute")
    private Integer focusMinute;

    // TODO: 추후 UserBook 연관관계 추가 예정

    @Builder
    public ReadingSession(LocalDateTime endTime, Integer focusMinute) {
        this.endTime = endTime;
        this.focusMinute = focusMinute;
    }

    public void endSession(LocalDateTime endTime, Integer focusMinute) {
        this.endTime = endTime;
        this.focusMinute = focusMinute;
    }
}
