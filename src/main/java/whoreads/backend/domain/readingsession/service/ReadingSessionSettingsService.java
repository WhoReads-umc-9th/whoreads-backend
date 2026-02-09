package whoreads.backend.domain.readingsession.service;

import whoreads.backend.domain.readingsession.dto.BlockedAppItem;
import whoreads.backend.domain.readingsession.dto.ReadingSessionResponse;

import java.util.List;

public interface ReadingSessionSettingsService {

    ReadingSessionResponse.FocusBlockSetting getFocusBlockSetting(Long memberId);

    ReadingSessionResponse.FocusBlockSetting updateFocusBlockSetting(Long memberId, Boolean focusBlockEnabled);

    ReadingSessionResponse.WhiteNoiseSetting getWhiteNoiseSetting(Long memberId);

    ReadingSessionResponse.WhiteNoiseSetting updateWhiteNoiseSetting(Long memberId, Boolean whiteNoiseEnabled);

    ReadingSessionResponse.WhiteNoiseList getWhiteNoiseList();

    ReadingSessionResponse.BlockedApps getBlockedApps(Long memberId);

    ReadingSessionResponse.BlockedApps updateBlockedApps(Long memberId, List<BlockedAppItem> blockedApps);
}
