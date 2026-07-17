package whoreads.backend.domain.readingsession.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

public class ReadingSessionRequest {

    @Getter
    @NoArgsConstructor
    public static class Start {
        @JsonProperty("member_id")
        @NotNull(message = "member_id는 필수입니다.")
        private Long memberId;
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
