package whoreads.backend.domain.readingsession.dto;

import lombok.Builder;
import lombok.Getter;
import whoreads.backend.domain.readingsession.enums.SessionStatus;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class ReadingSessionResponse {

    @Getter
    @Builder
    public static class StartResult {
        private Long sessionId;
    }

    @Getter
    @Builder
    public static class SessionDetail {
        private Long sessionId;
        private SessionStatus status;
        private Integer totalMinutes;
        private LocalDateTime createdAt;
        private LocalDateTime finishedAt;
        private List<IntervalInfo> intervals;
    }

    @Getter
    @Builder
    public static class IntervalInfo {
        private Long intervalId;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private Integer durationMinutes;
    }

    @Getter
    @Builder
    public static class TodayFocus {
        private Integer todayMinutes;
        private Integer differenceFromYesterday;
    }

    @Getter
    @Builder
    public static class TotalFocus {
        private Long totalMinutes;
    }

    @Getter
    @Builder
    public static class MonthlyRecords {
        private List<DailyRecord> records;
    }

    @Getter
    @Builder
    public static class DailyRecord {
        private Integer day;
        private LocalTime startTime;
        private LocalTime endTime;
        private Integer totalMinutes;
    }
}
