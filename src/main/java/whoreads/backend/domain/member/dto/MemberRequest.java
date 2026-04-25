package whoreads.backend.domain.member.dto;

import tools.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public class MemberRequest {
    public record FcmTokenRequest(
            @JsonProperty("fcm_token")
            @NotBlank
            String fcmToken
    ){}
}
