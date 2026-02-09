package whoreads.backend.domain.member.dto;

import jakarta.validation.constraints.NotBlank;

public class MemberRequest {
    public record FcmTokenRequest(
            @NotBlank
            String fcmToken
    ){}
}
