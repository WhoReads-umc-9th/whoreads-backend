package whoreads.backend.domain.member.dto;

import tools.jackson.annotation.JsonProperty;
import lombok.Builder;
import whoreads.backend.domain.dna.enums.TrackCode;
import whoreads.backend.domain.member.enums.AgeGroup;
import whoreads.backend.domain.member.enums.Gender;
import whoreads.backend.domain.member.enums.Status;

import java.time.LocalDateTime;

public class MemberResDto {

    @Builder
    public record MemberInfo(
            Long id,
            String nickname,
            Gender gender,
            @JsonProperty("age_group") AgeGroup ageGroup,
            String email,
            @JsonProperty("login_id") String loginId,
            @JsonProperty("created_at") LocalDateTime createdAt,
            @JsonProperty("updated_at") LocalDateTime updatedAt,
            @JsonProperty("deleted_at") LocalDateTime deletedAt,
            Status status,
            @JsonProperty("track_code") TrackCode trackCode,
            @JsonProperty("fcm_token") String fcmToken
    ){}

    @Builder
    public record CelebrityFollow(
            Long id,
            String name,
            @JsonProperty("image_url") String imageUrl,
            @JsonProperty("short_bio") String shortBio
    ) {}
}
