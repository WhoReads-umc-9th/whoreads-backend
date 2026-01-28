package whoreads.backend.domain.celebrity.dto;

import lombok.Builder;
import lombok.Getter;
import whoreads.backend.domain.celebrity.entity.Celebrity;
import whoreads.backend.domain.celebrity.entity.CelebrityTag;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class CelebrityResponse {
    private Long id;
    private String name;
    private String imageUrl;
    private String shortBio;
    private List<String> jobTags; // 가수, 배우 처럼 한글로 변환해서 전달

    public static CelebrityResponse from(Celebrity celebrity) {
        return CelebrityResponse.builder()
                .id(celebrity.getId())
                .name(celebrity.getName())
                .imageUrl(celebrity.getImageUrl())
                .shortBio(celebrity.getShortBio())
                .jobTags(celebrity.getJobTags().stream()
                        .map(CelebrityTag::getDescription)
                        .collect(Collectors.toList())
                )
                .build();
    }
}