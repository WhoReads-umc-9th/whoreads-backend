package whoreads.backend.domain.notification.dto;

import lombok.Builder;
import lombok.Getter;
import whoreads.backend.domain.notification.enums.NotificationType;
import whoreads.backend.domain.notification.event.NotificationEvent;

import java.util.Map;

@Getter
@Builder
public class FcmMessageDTO {
    private final String title;
    private final String body;
    private final Long celebrityId;
    private final Long bookId;
    private final String type;
    private final Map<String, String> data;

    /*
    todo: 링크 처리하는 것 정하기!
    * */
    public static FcmMessageDTO of(NotificationType type, NotificationEvent event) {
        String[] generated = type.generateMessage(event);

        FcmMessageDTO.FcmMessageDTOBuilder builder = FcmMessageDTO.builder()
                .title(generated[0])
                .body(generated[1])
                .type(type.name()); // DB 저장용 타입 이름 (FOLLOW, ROUTINE 등)

        if (event != null && type.equals(NotificationType.FOLLOW) &&
                event instanceof NotificationEvent.FollowEvent followEvent)
        {
            builder.celebrityId(followEvent.celebId());
            builder.bookId(followEvent.bookId());
        }
        return builder.build();
    }
}