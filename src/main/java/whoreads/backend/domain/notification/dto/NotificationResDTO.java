package whoreads.backend.domain.notification.dto;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

// NULL 인 필드 보내지 않기
public class NotificationResDTO {
    // 알림 리스트 조회
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record TotalSettingDTO(
            SettingDTO followSetting,
            List<SettingDTO> routineSettings
    ){}
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record SettingDTO(
            Long id,
            @Schema(description = "알림 시간", example = "12:16", type = "string")
            @JsonFormat(pattern = "HH:mm")
            String type,
            LocalTime time,
            List<DayOfWeek> days,
            boolean isEnabled
    ){}
}
