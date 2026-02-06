package whoreads.backend.domain.notification.dto;

import lombok.Builder;
import lombok.Getter;
import whoreads.backend.domain.notification.enums.NotificationType;
import whoreads.backend.domain.notification.event.NotificationEvent;
import whoreads.backend.global.exception.CustomException;
import whoreads.backend.global.exception.ErrorCode;

import java.util.Map;

@Getter
@Builder
public class FcmMessageDTO {
    private final String title;
    private final String body;
    private final String link;
    private final String type;
    private final Map<String, String> data;

    /*
    todo: 링크 처리하는 것 정하기!
    * */
    public static FcmMessageDTO of(NotificationType type, NotificationEvent event) {
        String[] generated = type.generateMessage(event);
        return FcmMessageDTO.builder()
                .title(generated[0])
                .body(generated[1])
                .type(type.name()) // DB 저장용 타입 이름 (FOLLOW, ROUTINE 등)
                .link(null)
                .build();
    }
}