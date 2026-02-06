package whoreads.backend.domain.readingsession.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import whoreads.backend.domain.readingsession.dto.ReadingSessionRequest;
import whoreads.backend.domain.readingsession.dto.ReadingSessionResponse;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReadingSessionSettingsServiceImpl implements ReadingSessionSettingsService {

    @Override
    public ReadingSessionResponse.FocusBlockSetting getFocusBlockSetting(Long memberId) {
        // TODO: 실제 구현 시
        // 1. memberId로 사용자 조회
        // 2. Member.focusBlockEnabled 값 반환

        // Mock: 기본값 false
        return ReadingSessionResponse.FocusBlockSetting.builder()
                .focusBlockEnabled(false)
                .build();
    }

    @Override
    public ReadingSessionResponse.FocusBlockSetting updateFocusBlockSetting(Long memberId, Boolean focusBlockEnabled) {
        // TODO: 실제 구현 시
        // 1. memberId로 사용자 조회
        // 2. Member.focusBlockEnabled 업데이트
        // 3. 변경된 값 반환

        // Mock: 요청값 그대로 반환
        return ReadingSessionResponse.FocusBlockSetting.builder()
                .focusBlockEnabled(focusBlockEnabled)
                .build();
    }

    @Override
    public ReadingSessionResponse.WhiteNoiseSetting getWhiteNoiseSetting(Long memberId) {
        // TODO: 실제 구현 시
        // 1. memberId로 사용자 조회
        // 2. Member.whiteNoiseEnabled 값 반환

        // Mock: 기본값 false
        return ReadingSessionResponse.WhiteNoiseSetting.builder()
                .whiteNoiseEnabled(false)
                .build();
    }

    @Override
    public ReadingSessionResponse.WhiteNoiseSetting updateWhiteNoiseSetting(Long memberId, Boolean whiteNoiseEnabled) {
        // TODO: 실제 구현 시
        // 1. memberId로 사용자 조회
        // 2. Member.whiteNoiseEnabled 업데이트
        // 3. 변경된 값 반환

        // Mock: 요청값 그대로 반환
        return ReadingSessionResponse.WhiteNoiseSetting.builder()
                .whiteNoiseEnabled(whiteNoiseEnabled)
                .build();
    }

    @Override
    public ReadingSessionResponse.WhiteNoiseList getWhiteNoiseList() {
        // TODO: 실제 구현 시
        // DB 또는 S3에서 백색소음 목록 조회

        // Mock 데이터
        List<ReadingSessionResponse.WhiteNoiseItem> items = List.of(
                ReadingSessionResponse.WhiteNoiseItem.builder()
                        .id(1L)
                        .name("빗소리")
                        .audioUrl("https://example.com/audio/rain.mp3")
                        .build(),
                ReadingSessionResponse.WhiteNoiseItem.builder()
                        .id(2L)
                        .name("파도소리")
                        .audioUrl("https://example.com/audio/wave.mp3")
                        .build(),
                ReadingSessionResponse.WhiteNoiseItem.builder()
                        .id(3L)
                        .name("카페 소음")
                        .audioUrl("https://example.com/audio/cafe.mp3")
                        .build(),
                ReadingSessionResponse.WhiteNoiseItem.builder()
                        .id(4L)
                        .name("모닥불")
                        .audioUrl("https://example.com/audio/fire.mp3")
                        .build()
        );

        return ReadingSessionResponse.WhiteNoiseList.builder()
                .items(items)
                .build();
    }

    @Override
    public ReadingSessionResponse.BlockedApps getBlockedApps(Long memberId) {
        // TODO: 실제 구현 시
        // 1. memberId로 사용자 조회
        // 2. 사용자의 차단 앱 목록 조회

        // Mock 데이터
        List<ReadingSessionResponse.BlockedAppItem> blockedApps = List.of(
                ReadingSessionResponse.BlockedAppItem.builder()
                        .bundleId("com.burbn.instagram")
                        .name("Instagram")
                        .build(),
                ReadingSessionResponse.BlockedAppItem.builder()
                        .bundleId("com.google.ios.youtube")
                        .name("YouTube")
                        .build()
        );

        return ReadingSessionResponse.BlockedApps.builder()
                .blockedApps(blockedApps)
                .build();
    }

    @Override
    public ReadingSessionResponse.BlockedApps updateBlockedApps(Long memberId, List<ReadingSessionRequest.BlockedAppItem> blockedApps) {
        if (blockedApps == null) {
            blockedApps = List.of();
        }
        // TODO: 실제 구현 시
        // 1. memberId로 사용자 조회
        // 2. 기존 차단 앱 목록 삭제
        // 3. 새 차단 앱 목록 저장

        // Mock: 요청값 그대로 반환
        List<ReadingSessionResponse.BlockedAppItem> responseItems = blockedApps.stream()
                .map(app -> ReadingSessionResponse.BlockedAppItem.builder()
                        .bundleId(app.getBundleId())
                        .name(app.getName())
                        .build())
                .toList();

        return ReadingSessionResponse.BlockedApps.builder()
                .blockedApps(responseItems)
                .build();
    }
}
