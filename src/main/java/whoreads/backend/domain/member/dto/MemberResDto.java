package whoreads.backend.domain.member.dto;

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
            AgeGroup ageGroup,
            String email,
            String loginId,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            LocalDateTime deletedAt,
            Status status,
            TrackCode trackCode,
            String fcmToken
    ){}

    @Builder
    public record CelebrityFollow(
            Long id,
            String name,
            String imageUrl,
            String shortBio
    ) {}
}
