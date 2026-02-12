package whoreads.backend.domain.readingsession.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import whoreads.backend.domain.focusmode.entity.BlockedApp;
import whoreads.backend.domain.focusmode.entity.FocusTimerSetting;
import whoreads.backend.domain.focusmode.entity.WhiteNoise;
import whoreads.backend.domain.focusmode.repository.BlockedAppRepository;
import whoreads.backend.domain.focusmode.repository.FocusModeRepository;
import whoreads.backend.domain.focusmode.repository.WhiteNoiseRepository;
import whoreads.backend.domain.member.entity.Member;
import whoreads.backend.domain.member.repository.MemberRepository;
import whoreads.backend.domain.readingsession.dto.BlockedAppItem;
import whoreads.backend.domain.readingsession.dto.ReadingSessionResponse;
import whoreads.backend.domain.readingsession.dto.WhiteNoiseItem;
import whoreads.backend.global.exception.CustomException;
import whoreads.backend.global.exception.ErrorCode;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReadingSessionSettingsServiceImpl implements ReadingSessionSettingsService {

    private final FocusModeRepository focusModeRepository;
    private final BlockedAppRepository blockedAppRepository;
    private final WhiteNoiseRepository whiteNoiseRepository;
    private final MemberRepository memberRepository;

    @Override
    public ReadingSessionResponse.FocusBlockSetting getFocusBlockSetting(Long memberId) {
        FocusTimerSetting setting = getOrCreateSetting(memberId);

        return ReadingSessionResponse.FocusBlockSetting.builder()
                .focusBlockEnabled(setting.getFocusBlockEnabled())
                .build();
    }

    @Override
    @Transactional
    public ReadingSessionResponse.FocusBlockSetting updateFocusBlockSetting(Long memberId, Boolean focusBlockEnabled) {
        FocusTimerSetting setting = getOrCreateSetting(memberId);
        setting.updateFocusBlockEnabled(focusBlockEnabled);

        return ReadingSessionResponse.FocusBlockSetting.builder()
                .focusBlockEnabled(setting.getFocusBlockEnabled())
                .build();
    }

    @Override
    public ReadingSessionResponse.WhiteNoiseSetting getWhiteNoiseSetting(Long memberId) {
        FocusTimerSetting setting = getOrCreateSetting(memberId);

        return ReadingSessionResponse.WhiteNoiseSetting.builder()
                .whiteNoiseEnabled(setting.getWhiteNoiseEnabled())
                .build();
    }

    @Override
    @Transactional
    public ReadingSessionResponse.WhiteNoiseSetting updateWhiteNoiseSetting(Long memberId, Boolean whiteNoiseEnabled) {
        FocusTimerSetting setting = getOrCreateSetting(memberId);
        setting.updateWhiteNoiseEnabled(whiteNoiseEnabled);

        return ReadingSessionResponse.WhiteNoiseSetting.builder()
                .whiteNoiseEnabled(setting.getWhiteNoiseEnabled())
                .build();
    }

    @Override
    public ReadingSessionResponse.WhiteNoiseList getWhiteNoiseList() {
        List<WhiteNoiseItem> items = whiteNoiseRepository.findAll().stream()
                .map(wn -> WhiteNoiseItem.builder()
                        .id(wn.getId())
                        .name(wn.getName())
                        .audioUrl(wn.getAudioUrl())
                        .build())
                .toList();

        return ReadingSessionResponse.WhiteNoiseList.builder()
                .items(items)
                .build();
    }

    @Override
    public ReadingSessionResponse.BlockedApps getBlockedApps(Long memberId) {
        List<BlockedAppItem> blockedApps = blockedAppRepository.findByMemberId(memberId).stream()
                .map(app -> BlockedAppItem.builder()
                        .bundleId(app.getBundleId())
                        .name(app.getName())
                        .build())
                .toList();

        return ReadingSessionResponse.BlockedApps.builder()
                .blockedApps(blockedApps)
                .build();
    }

    @Override
    @Transactional
    public ReadingSessionResponse.BlockedApps updateBlockedApps(Long memberId, List<BlockedAppItem> blockedApps) {
        if (blockedApps == null) {
            blockedApps = List.of();
        }

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        // 기존 차단 앱 삭제 후 새로 저장
        blockedAppRepository.deleteByMemberId(memberId);

        List<BlockedApp> newApps = blockedApps.stream()
                .map(item -> BlockedApp.builder()
                        .member(member)
                        .bundleId(item.getBundleId())
                        .name(item.getName())
                        .build())
                .toList();
        blockedAppRepository.saveAll(newApps);

        List<BlockedAppItem> result = newApps.stream()
                .map(app -> BlockedAppItem.builder()
                        .bundleId(app.getBundleId())
                        .name(app.getName())
                        .build())
                .toList();

        return ReadingSessionResponse.BlockedApps.builder()
                .blockedApps(result)
                .build();
    }

    private FocusTimerSetting getOrCreateSetting(Long memberId) {
        return focusModeRepository.findByMemberId(memberId)
                .orElseGet(() -> {
                    Member member = memberRepository.findById(memberId)
                            .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
                    return focusModeRepository.save(FocusTimerSetting.builder()
                            .member(member)
                            .build());
                });
    }
}
