package whoreads.backend.domain.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MemberToken implements MemberTokenDTO {
    Long memberId;
    String fcmToken;
}
