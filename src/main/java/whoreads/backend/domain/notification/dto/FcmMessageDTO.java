package whoreads.backend.domain.notification.dto;

import lombok.Builder;
import lombok.Getter;
import whoreads.backend.domain.notification.enums.NotificationType;
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

    // NotificationTypes(상위 타입)를 받아서 메시지를 생성하도록 변경
    public static FcmMessageDTO of(NotificationType type, String link, Object... args) {
        String[] generated = type.generateMessage(args);
        if (generated == null || generated.length < 2){
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR,"제목과 본문이 존재하지 않습니다.");
        }

        return FcmMessageDTO.builder()
                .title(generated[0])
                .body(generated[1])
                .type(type.name()) // DB 저장용 타입 이름 (FOLLOW, ROUTINE 등)
                .link(link)
                .build();
    }
}