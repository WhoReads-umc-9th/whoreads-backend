package whoreads.backend.domain.notification.converter;

import whoreads.backend.domain.notification.dto.NotificationResDTO;
import whoreads.backend.domain.notification.entity.Notification;
import whoreads.backend.domain.notification.enums.NotificationType;

import java.util.List;

public class NotificationConverter {

    // 모든 타입을 이 메서드 하나로 변환합니다.
    public static NotificationResDTO.SettingDTO toSettingDTO(Notification notification) {
        return NotificationResDTO.SettingDTO.builder()
                .id(notification.getId())
                .type(notification.getType().name())
                .time(notification.getTime()) // FOLLOW면 알아서 null로 들어감
                .days(notification.getDays()) // FOLLOW면 알아서 null로 들어감
                .isEnabled(notification.isEnabled())
                .build();
    }

    // 전체 목록 조회 시 사용
    public static NotificationResDTO.TotalSettingDTO toTotalSettingDTO(List<Notification> notifications) {
        // 1. FOLLOW 타입 찾기 (0번 찍기)
        NotificationResDTO.SettingDTO follow = notifications.stream()
                .filter(n -> n.getType() == NotificationType.FOLLOW)
                .findFirst()
                .map(NotificationConverter::toSettingDTO)
                .orElse(null);

        // 2. ROUTINE 타입들 찾기
        List<NotificationResDTO.SettingDTO> routines = notifications.stream()
                .filter(n -> n.getType() == NotificationType.ROUTINE)
                .map(NotificationConverter::toSettingDTO)
                .toList();

        return NotificationResDTO.TotalSettingDTO.builder()
                .followSetting(follow)
                .routineSettings(routines)
                .build();
    }
}
