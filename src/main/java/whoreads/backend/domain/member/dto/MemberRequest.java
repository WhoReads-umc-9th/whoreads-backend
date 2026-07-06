package whoreads.backend.domain.member.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import whoreads.backend.domain.member.enums.AgeGroup;
import whoreads.backend.domain.member.enums.Gender;

public class MemberRequest {
    public record FcmTokenRequest(
            @JsonProperty("fcm_token")
            @NotBlank
            String fcmToken
    ){}

    public record UpdateNicknameRequest(
            @NotBlank(message = "닉네임은 필수 입력 항목입니다.")
            String nickname
    ){}

    public record UpdateGenderRequest(
            @NotNull(message = "성별은 필수 입력 항목입니다.")
            Gender gender
    ){}

    public record UpdateAgeRequest(
            @NotNull(message = "연령대는 필수 입력 항목입니다.")
            AgeGroup ageGroup
    ){}
}
