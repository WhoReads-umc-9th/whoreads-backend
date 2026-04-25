package whoreads.backend.domain.notification.dto;

import tools.jackson.annotation.JsonFormat;
import tools.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import whoreads.backend.domain.notification.enums.NotificationType;
import whoreads.backend.global.exception.CustomException;
import whoreads.backend.global.exception.ErrorCode;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

public class NotificationReqDTO {
    @Builder
    public record SettingDTO(
            @Schema(description = "알림 시간", example = "12:16", type = "string")
            @JsonFormat(pattern = "HH:mm")
            LocalTime time,

            @NotNull(message = "알림 타입은 필수 입력 값입니다.")
            NotificationType type,

            @JsonProperty("is_enabled")
            boolean isEnabled,

            List<DayOfWeek> days
    ) {
        public void validate() {
            if (this.type == NotificationType.ROUTINE) {
                if (this.time == null) throw new CustomException(ErrorCode.INVALID_INPUT_VALUE,"루틴 알림은 시간이 필수입니다.");
                if (this.days == null || this.days.isEmpty()) throw new CustomException(ErrorCode.INVALID_INPUT_VALUE,"루틴 알림은 요일 설정이 필수입니다.");
            }
        }
    }
}
