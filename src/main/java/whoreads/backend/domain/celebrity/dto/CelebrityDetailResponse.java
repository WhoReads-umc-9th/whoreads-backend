package whoreads.backend.domain.celebrity.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import whoreads.backend.domain.celebrity.entity.Celebrity;
import whoreads.backend.domain.celebrity.entity.CelebrityTag;

import java.util.List;
import java.util.stream.Collectors;

/** 유명인 상세 조회(GET /api/celebrities/{id}) 전용 — 출처 표기 블록은 상세 조회에서만 내려준다. */
@Getter
@Builder
public class CelebrityDetailResponse {
    private Long id;
    private String name;
    @JsonProperty("image_url") private String imageUrl;
    @JsonProperty("image_attribution") private String imageAttribution;
    @JsonProperty("short_bio") private String shortBio;
    @JsonProperty("job_tags") private List<String> jobTags;

    /** @param imageUrl S3Service.generateUrl()로 변환을 마친 절대 URL */
    public static CelebrityDetailResponse from(Celebrity celebrity, String imageUrl) {
        return CelebrityDetailResponse.builder()
                .id(celebrity.getId())
                .name(celebrity.getName())
                .imageUrl(imageUrl)
                .imageAttribution(celebrity.getImageAttribution())
                .shortBio(celebrity.getShortBio())
                .jobTags(celebrity.getJobTags().stream()
                        .map(CelebrityTag::getDescription)
                        .collect(Collectors.toList())
                )
                .build();
    }
}
