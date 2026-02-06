package whoreads.backend.domain.readingsession.service;

import whoreads.backend.domain.readingsession.dto.ReadingSessionRequest;
import whoreads.backend.domain.readingsession.dto.ReadingSessionResponse;

public interface ReadingSessionSettingsService {

    ReadingSessionResponse.FocusBlockSetting getFocusBlockSetting();

    ReadingSessionResponse.FocusBlockSetting updateFocusBlockSetting(Boolean focusBlockEnabled);

    ReadingSessionResponse.WhiteNoiseSetting getWhiteNoiseSetting();

    ReadingSessionResponse.WhiteNoiseSetting updateWhiteNoiseSetting(Boolean whiteNoiseEnabled);

    ReadingSessionResponse.WhiteNoiseList getWhiteNoiseList();

    ReadingSessionResponse.BlockedApps getBlockedApps();

import whoreads.backend.domain.readingsession.dto.ReadingSessionRequest;
import whoreads.backend.domain.readingsession.dto.ReadingSessionResponse;
import java.util.List;

public interface ReadingSessionSettingsService {
    ReadingSessionResponse.BlockedApps updateBlockedApps(List<ReadingSessionRequest.BlockedAppItem> blockedApps);
}
}
