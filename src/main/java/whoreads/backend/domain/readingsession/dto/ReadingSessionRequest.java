package whoreads.backend.domain.readingsession.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

public class ReadingSessionRequest {

    @Getter
    @NoArgsConstructor
    public static class Start {
        @NotNull(message = "memberId는 필수입니다.")
        private Long memberId;
    }

    @Getter
    @NoArgsConstructor
    public static class UpdateFocusBlock {
        @NotNull(message = "focus_block_enabled는 필수입니다.")
        private Boolean focusBlockEnabled;
    }

    @Getter
    @NoArgsConstructor
    public static class UpdateWhiteNoise {
        @NotNull(message = "white_noise_enabled는 필수입니다.")
        private Boolean whiteNoiseEnabled;
    }

    @Getter
    @NoArgsConstructor
    public static class UpdateBlockedApps {
        @NotNull(message = "blocked_apps는 필수입니다.")
        @Size(max = 100, message = "차단 앱은 최대 100개까지 등록할 수 있습니다.")
        @Valid
        private List<BlockedAppItem> blockedApps;
    }
}
