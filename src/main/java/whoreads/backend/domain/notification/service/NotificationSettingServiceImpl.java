package whoreads.backend.domain.notification.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import whoreads.backend.domain.member.entity.Member;
import whoreads.backend.domain.member.repository.MemberRepository;
import whoreads.backend.domain.notification.converter.NotificationConverter;
import whoreads.backend.domain.notification.dto.NotificationReqDTO;
import whoreads.backend.domain.notification.dto.NotificationResDTO;
import whoreads.backend.domain.notification.entity.NotificationSetting;
import whoreads.backend.domain.notification.enums.NotificationType;
import whoreads.backend.domain.notification.repository.NotificationSettingRepository;
import whoreads.backend.global.exception.CustomException;
import whoreads.backend.global.exception.ErrorCode;

import java.util.List;


@Service
@RequiredArgsConstructor
public class NotificationSettingServiceImpl implements NotificationSettingService {
    private final NotificationSettingRepository notificationSettingRepository;
    private final MemberRepository memberRepository;

    @Override
    @Transactional
    public NotificationResDTO.TotalSettingDTO getNotificationSettings(Long userId, String typeStr) {
        NotificationType type = null;
        if (typeStr != null) {
            try {
                type = NotificationType.valueOf(typeStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new CustomException(ErrorCode.METHOD_NOT_ALLOWED, "알림 타입이 올바르지 않습니다");
            }
        }
        List<NotificationSetting> notifications = notificationSettingRepository.findAllByUserIdAndOptionalType(userId, type);

        // 팔로우 설정이 없는 경우 추가 (사용자가 처음 진입한 경우)
        boolean hasFollow = notifications.stream()
                .anyMatch(n -> n.getType() == NotificationType.FOLLOW);

        if (!hasFollow && (type == null || type == NotificationType.FOLLOW)) {
            // 유저 조회
            Member member = memberRepository.findById(userId)
                    .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

            // 팔로우 알림 기본값 등록
            NotificationSetting defaultFollow = NotificationSetting.builder()
                    .member(member)
                    .type(NotificationType.FOLLOW)
                    .isEnabled(false)
                    .build();

            NotificationSetting savedFollow = notificationSettingRepository.save(defaultFollow);

            // 기존 리스트에 추가
            notifications.add(savedFollow);
        }
        return NotificationConverter.toTotalSettingDTO(notifications);
    }

    @Override
    @Transactional
    public NotificationResDTO.SettingDTO createNotificationSetting(Long memberId, NotificationReqDTO.SettingDTO createSettingDTO) {
        // 1. DTO 자체 검증 로직 실행 (ROUTINE일 때 시간/요일 체크)
        createSettingDTO.validate();

        // 2. 해당 유저 존재 여부 확인
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        NotificationSetting notification;

        // 3. 타입에 따른 분기 처리
        if (createSettingDTO.type() == NotificationType.FOLLOW) {
            // [FOLLOW] 유저당 하나만 존재
            // 기존 것이 있으면 가져오고, 없으면 새로 생성
            notification = notificationSettingRepository.findAllByUserIdAndOptionalType(memberId, NotificationType.FOLLOW)
                    .stream()
                    .findFirst()
                    .orElseGet(() -> NotificationSetting.builder()
                            .member(member)
                            .type(NotificationType.FOLLOW)
                            .build());
            notification.updateEnabled(createSettingDTO.isEnabled());
        } else {
            // [ROUTINE] 루틴 알림은 여러 개 생성 가능
            notification = NotificationSetting.builder()
                    .member(member)
                    .type(NotificationType.ROUTINE)
                    .time(createSettingDTO.time())
                    .days(createSettingDTO.days())
                    .isEnabled(true)
                    .build();
        }

        // 4. 저장 및 DTO 변환 반환
        notification = notificationSettingRepository.save(notification);
        return NotificationConverter.toSettingDTO(notification);
    }

    @Override
    public NotificationResDTO.SettingDTO updateNotificationSetting(Long userId, Long notificationId, NotificationReqDTO.SettingDTO updateSettingDTO) {
        updateSettingDTO.validate();
        NotificationSetting notification = notificationSettingRepository.findById(notificationId)
                .orElseThrow(()-> new CustomException(ErrorCode.RESOURCE_NOT_FOUND,"알림을 찾을 수 없습니다."));
        if (!notification.getMember().getId().equals(userId))
            throw new CustomException(ErrorCode.ACCESS_DENIED,"사용자의 알림이 아닙니다.");
        if (notification.getType() != updateSettingDTO.type())
            throw new CustomException(ErrorCode.METHOD_NOT_ALLOWED,"타입 변환은 불가합니다.");
        if (notification.getType() == NotificationType.FOLLOW) {
            // 팔로우 알림은 활성화 여부만 업데이트
            notification.updateEnabled(updateSettingDTO.isEnabled());
        } else {
            // 루틴 알림은 시간, 요일, 활성화 여부 모두 업데이트
            notification.updateRoutine(
                    updateSettingDTO.time(),
                    updateSettingDTO.days(),
                    updateSettingDTO.isEnabled()
            );
        }
        notificationSettingRepository.save(notification);
        return NotificationConverter.toSettingDTO(notification);
    }
    @Override
    public void deleteNotificationSetting(Long userId, Long notificationSettingId) {
        NotificationSetting notification = notificationSettingRepository.findById(notificationSettingId)
                .orElseThrow(()-> new CustomException(ErrorCode.RESOURCE_NOT_FOUND,"알림을 찾을 수 없습니다."));
        if (!notification.getMember().getId().equals(userId))
            throw new CustomException(ErrorCode.ACCESS_DENIED,"사용자의 알림이 아닙니다.");
        notificationSettingRepository.deleteById(notificationSettingId);
    }
}
