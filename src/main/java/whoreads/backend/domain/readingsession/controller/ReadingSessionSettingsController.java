package whoreads.backend.domain.readingsession.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import whoreads.backend.domain.readingsession.controller.docs.ReadingSessionSettingsControllerDocs;
import whoreads.backend.domain.readingsession.dto.ReadingSessionRequest;
import whoreads.backend.domain.readingsession.dto.ReadingSessionResponse;
import whoreads.backend.domain.readingsession.service.ReadingSessionSettingsService;
import whoreads.backend.global.response.ApiResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/me/reading-sessions/settings")
public class ReadingSessionSettingsController implements ReadingSessionSettingsControllerDocs {

    private final ReadingSessionSettingsService readingSessionSettingsService;

    @Override
    @GetMapping("/focus-block")
    public ResponseEntity<ApiResponse<ReadingSessionResponse.FocusBlockSetting>> getFocusBlockSetting(
            @AuthenticationPrincipal Long memberId
    ) {
        ReadingSessionResponse.FocusBlockSetting result = readingSessionSettingsService.getFocusBlockSetting(memberId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @Override
    @PatchMapping("/focus-block")
    public ResponseEntity<ApiResponse<ReadingSessionResponse.FocusBlockSetting>> updateFocusBlockSetting(
            @AuthenticationPrincipal Long memberId,
            @Valid @RequestBody ReadingSessionRequest.UpdateFocusBlock request
    ) {
        ReadingSessionResponse.FocusBlockSetting result = readingSessionSettingsService.updateFocusBlockSetting(memberId, request.getFocusBlockEnabled());
        return ResponseEntity.ok(ApiResponse.success("집중 차단 모드 설정이 변경되었습니다.", result));
    }

    @Override
    @GetMapping("/white-noise")
    public ResponseEntity<ApiResponse<ReadingSessionResponse.WhiteNoiseSetting>> getWhiteNoiseSetting(
            @AuthenticationPrincipal Long memberId
    ) {
        ReadingSessionResponse.WhiteNoiseSetting result = readingSessionSettingsService.getWhiteNoiseSetting(memberId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @Override
    @PatchMapping("/white-noise")
    public ResponseEntity<ApiResponse<ReadingSessionResponse.WhiteNoiseSetting>> updateWhiteNoiseSetting(
            @AuthenticationPrincipal Long memberId,
            @Valid @RequestBody ReadingSessionRequest.UpdateWhiteNoise request
    ) {
        ReadingSessionResponse.WhiteNoiseSetting result = readingSessionSettingsService.updateWhiteNoiseSetting(memberId, request.getWhiteNoiseEnabled());
        return ResponseEntity.ok(ApiResponse.success("백색소음 설정이 변경되었습니다.", result));
    }

    @Override
    @GetMapping("/white-noise/list")
    public ResponseEntity<ApiResponse<ReadingSessionResponse.WhiteNoiseList>> getWhiteNoiseList() {
        ReadingSessionResponse.WhiteNoiseList result = readingSessionSettingsService.getWhiteNoiseList();
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @Override
    @GetMapping("/blocked-apps")
    public ResponseEntity<ApiResponse<ReadingSessionResponse.BlockedApps>> getBlockedApps(
            @AuthenticationPrincipal Long memberId
    ) {
        ReadingSessionResponse.BlockedApps result = readingSessionSettingsService.getBlockedApps(memberId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @Override
    @PutMapping("/blocked-apps")
    public ResponseEntity<ApiResponse<ReadingSessionResponse.BlockedApps>> updateBlockedApps(
            @AuthenticationPrincipal Long memberId,
            @Valid @RequestBody ReadingSessionRequest.UpdateBlockedApps request
    ) {
        ReadingSessionResponse.BlockedApps result = readingSessionSettingsService.updateBlockedApps(memberId, request.getBlockedApps());
        return ResponseEntity.ok(ApiResponse.success("차단 앱 목록이 저장되었습니다.", result));
    }
}
