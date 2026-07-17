package whoreads.backend.domain.notification.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import whoreads.backend.domain.notification.entity.FollowLink;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class NotificationResDTO {

    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record TotalSettingDTO(
            @JsonProperty("follow_setting") SettingDTO followSetting,
            @JsonProperty("routine_settings") List<SettingDTO> routineSettings
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
            @JsonProperty("is_enabled") boolean isEnabled
    ){}

    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record TotalInboxDTO(
            List<HistoryDTO> contents,
            @JsonProperty("next_cursor") Long nextCursor,
            @JsonProperty("has_next") Boolean hasNext
    ){}

    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record HistoryDTO(
            Long id,
            String type,
            String title,
            String body,
            FollowLink link,
            @JsonProperty("is_read") boolean isRead,
            @JsonProperty("created_at")
            @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
            LocalDateTime createdAt
    ){}
}
