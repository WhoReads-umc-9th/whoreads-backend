package whoreads.backend.domain.readingsession.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
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
        private Long totalMinutes;
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
        private Long durationMinutes;
    }

    @Getter
    @Builder
    public static class TodayFocus {
        private Long todayMinutes;
        private Long differenceFromYesterday;
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
        @JsonFormat(pattern = "HH:mm")
        private LocalTime startTime;
        @JsonFormat(pattern = "HH:mm")
        private LocalTime endTime;
        private Long totalMinutes;
    }

    @Getter
    @Builder
    public static class FocusBlockSetting {
        private Boolean focusBlockEnabled;
    }

    @Getter
    @Builder
    public static class WhiteNoiseSetting {
        private Boolean whiteNoiseEnabled;
    }

    @Getter
    @Builder
    public static class WhiteNoiseList {
        private List<WhiteNoiseItem> items;
    }

    @Getter
    @Builder
    public static class BlockedApps {
        private List<BlockedAppItem> blockedApps;
    }
}
