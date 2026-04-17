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

    @Getter
    @Builder
    public static class SessionSettings { // 추가 - 현
        private Long timerMinutes;
        private Boolean focusBlockEnabled;
        private Boolean whiteNoiseEnabled;
    }

    // 위에 SessionSettings 사용해도 될듯. 저기 있는 timerMinutes가 사용자가 설정한 시간인건가?
    @Getter
    @Builder
    public static class IncompleteResult {
        private Long sessionId;
        private String status;   // IN_PROGRESS, PAUSED, SUSPENDED
        private Long totalReadMinutes;
        private Long idleMinutes;   // IN_PROGRESS인 경우 마지막 세션 이후 경과 시간
        private Long remainingMinutes;

        // 집중 모드 설정 정보
        private Boolean focusBlockEnabled;
        private Boolean whiteNoiseEnabled;
    }
}
