package whoreads.backend.domain.readingsession.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
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
        @JsonProperty("session_id") private Long sessionId;
    }

    @Getter
    @Builder
    public static class SessionDetail {
        @JsonProperty("session_id") private Long sessionId;
        private SessionStatus status;
        @JsonProperty("total_minutes") private Long totalMinutes;
        @JsonProperty("created_at") private LocalDateTime createdAt;
        @JsonProperty("finished_at") private LocalDateTime finishedAt;
        private List<IntervalInfo> intervals;
    }

    @Getter
    @Builder
    public static class IntervalInfo {
        @JsonProperty("interval_id") private Long intervalId;
        @JsonProperty("start_time") private LocalDateTime startTime;
        @JsonProperty("end_time") private LocalDateTime endTime;
        @JsonProperty("duration_minutes") private Long durationMinutes;
    }

    @Getter
    @Builder
    public static class TodayFocus {
        @JsonProperty("today_minutes") private Long todayMinutes;
        @JsonProperty("difference_from_yesterday") private Long differenceFromYesterday;
    }

    @Getter
    @Builder
    public static class TotalFocus {
        @JsonProperty("total_minutes") private Long totalMinutes;
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
        @JsonProperty("start_time") @JsonFormat(pattern = "HH:mm") private LocalTime startTime;
        @JsonProperty("end_time") @JsonFormat(pattern = "HH:mm") private LocalTime endTime;
        @JsonProperty("total_minutes") private Long totalMinutes;
    }

    @Getter
    @Builder
    public static class FocusBlockSetting {
        @JsonProperty("focus_block_enabled") private Boolean focusBlockEnabled;
    }

    @Getter
    @Builder
    public static class WhiteNoiseSetting {
        @JsonProperty("white_noise_enabled") private Boolean whiteNoiseEnabled;
    }

    @Getter
    @Builder
    public static class WhiteNoiseList {
        private List<WhiteNoiseItem> items;
    }

    @Getter
    @Builder
    public static class BlockedApps {
        @JsonProperty("blocked_apps") private List<BlockedAppItem> blockedApps;
    }

    @Getter
    @Builder
    public static class SessionSettings {
        @JsonProperty("timer_minutes") private Long timerMinutes;
        @JsonProperty("focus_block_enabled") private Boolean focusBlockEnabled;
        @JsonProperty("white_noise_enabled") private Boolean whiteNoiseEnabled;
    }

    @Getter
    @Builder
    @JsonPropertyOrder({"session_id", "status", "total_read_minutes", "remaining_minutes", "idle_minutes", "focus_block_enabled", "white_noise_enabled"})
    public static class IncompleteResult {
        @JsonProperty("session_id") private Long sessionId;
        private String status;   // IN_PROGRESS, PAUSED, SUSPENDED
        @JsonProperty("total_read_minutes") private Long totalReadMinutes;
        @JsonProperty("remaining_minutes") private Long remainingMinutes;
        @JsonProperty("idle_minutes") private Long idleMinutes;   // IN_PROGRESS인 경우 마지막 세션 이후 경과 시간
        @JsonProperty("focus_block_enabled") private Boolean focusBlockEnabled;
        @JsonProperty("white_noise_enabled") private Boolean whiteNoiseEnabled;
    }

    @Getter
    @Builder
    public static class ResumeResult {
        private Long remainingMinutes;
    }
}
