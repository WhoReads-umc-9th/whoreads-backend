package whoreads.backend.domain.celebrity.dto;

import lombok.Builder;
import lombok.Getter;
import whoreads.backend.domain.celebrity.entity.Celebrity;
import whoreads.backend.domain.celebrity.entity.CelebrityTag;

@Getter
@Builder
public class CelebrityResponse {
    private Long id;
    private String name;
    private String imageUrl;
    private String shortBio;
    private String tags; // enum -> string

    public static CelebrityResponse from(Celebrity celebrity) {
        return CelebrityResponse.builder()
                .id(celebrity.getId())
                .name(celebrity.getName())
                .imageUrl(celebrity.getImageUrl())
                .shortBio(celebrity.getShortBio())
                .tags(celebrity.getTags().getDescription()) // 한글명 반환(ex : 작가, 아티스트)
                .build();
    }
}