package whoreads.backend.domain.celebrity.dto;

import tools.jackson.annotation.JsonProperty;
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
    @JsonProperty("image_url") private String imageUrl;
    @JsonProperty("short_bio") private String shortBio;
    @JsonProperty("job_tags") private List<String> jobTags;

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
