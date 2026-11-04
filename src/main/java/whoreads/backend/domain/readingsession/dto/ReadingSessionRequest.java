package whoreads.backend.domain.readingsession.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

public class ReadingSessionRequest {

    @Getter
    @NoArgsConstructor
    public static class Start {
        // 세 값 모두 선택값이며, 보내지 않으면 ReadingSession의 기본값이 적용된다 (body 자체를 생략해도 된다)
        @JsonProperty("heartbeat_interval_minutes")
        @Min(value = 1, message = "heartbeat_interval_minutes는 1분 이상이어야 합니다.")
        @Max(value = 1440, message = "heartbeat_interval_minutes는 1440분(24시간) 이하여야 합니다.")
        private Long heartbeatIntervalMinutes;

        @JsonProperty("suspend_after_minutes")
        @Min(value = 1, message = "suspend_after_minutes는 1분 이상이어야 합니다.")
        @Max(value = 1440, message = "suspend_after_minutes는 1440분(24시간) 이하여야 합니다.")
        private Long suspendAfterMinutes;

        @JsonProperty("complete_after_minutes")
        @Min(value = 1, message = "complete_after_minutes는 1분 이상이어야 합니다.")
        @Max(value = 1440, message = "complete_after_minutes는 1440분(24시간) 이하여야 합니다.")
        private Long completeAfterMinutes;
    }

    @Getter
    @NoArgsConstructor
    public static class UpdateFocusBlock {
        @JsonProperty("focus_block_enabled")
        @NotNull(message = "focus_block_enabled는 필수입니다.")
        private Boolean focusBlockEnabled;
    }

    @Getter
    @NoArgsConstructor
    public static class UpdateWhiteNoise {
        @JsonProperty("white_noise_enabled")
        @NotNull(message = "white_noise_enabled는 필수입니다.")
        private Boolean whiteNoiseEnabled;
    }

    @Getter
    @NoArgsConstructor
    public static class UpdateBlockedApps {
        @JsonProperty("blocked_apps")
        @NotNull(message = "blocked_apps는 필수입니다.")
        @Size(max = 100, message = "차단 앱은 최대 100개까지 등록할 수 있습니다.")
        @Valid
        private List<BlockedAppItem> blockedApps;
    }

    @Getter
    @NoArgsConstructor
    public static class TimerUpdate {
        private Long time;
    }
}
