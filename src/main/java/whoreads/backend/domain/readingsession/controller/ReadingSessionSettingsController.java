package whoreads.backend.domain.readingsession.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<ApiResponse<ReadingSessionResponse.FocusBlockSetting>> getFocusBlockSetting() {
        ReadingSessionResponse.FocusBlockSetting result = readingSessionSettingsService.getFocusBlockSetting();
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @Override
    @PatchMapping("/focus-block")
    public ResponseEntity<ApiResponse<ReadingSessionResponse.FocusBlockSetting>> updateFocusBlockSetting(
            @RequestBody ReadingSessionRequest.UpdateFocusBlock request
    ) {
        ReadingSessionResponse.FocusBlockSetting result = readingSessionSettingsService.updateFocusBlockSetting(request.getFocusBlockEnabled());
        return ResponseEntity.ok(ApiResponse.success("집중 차단 모드 설정이 변경되었습니다.", result));
    }

    @Override
    @GetMapping("/white-noise")
    public ResponseEntity<ApiResponse<ReadingSessionResponse.WhiteNoiseSetting>> getWhiteNoiseSetting() {
        ReadingSessionResponse.WhiteNoiseSetting result = readingSessionSettingsService.getWhiteNoiseSetting();
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @Override
    @PatchMapping("/white-noise")
    public ResponseEntity<ApiResponse<ReadingSessionResponse.WhiteNoiseSetting>> updateWhiteNoiseSetting(
            @RequestBody ReadingSessionRequest.UpdateWhiteNoise request
    ) {
        ReadingSessionResponse.WhiteNoiseSetting result = readingSessionSettingsService.updateWhiteNoiseSetting(request.getWhiteNoiseEnabled());
        return ResponseEntity.ok(ApiResponse.success("백색소음 설정이 변경되었습니다.", result));
    }

    @Override
    @GetMapping("/white-noise/list")
    public ResponseEntity<ApiResponse<ReadingSessionResponse.WhiteNoiseList>> getWhiteNoiseList() {
        ReadingSessionResponse.WhiteNoiseList result = readingSessionSettingsService.getWhiteNoiseList();
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
