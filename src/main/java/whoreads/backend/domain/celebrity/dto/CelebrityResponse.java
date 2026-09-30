package whoreads.backend.domain.celebrity.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import whoreads.backend.domain.celebrity.entity.Celebrity;
import whoreads.backend.domain.celebrity.entity.CelebrityTag;
import whoreads.backend.domain.celebrity.entity.ImageLicense;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class CelebrityResponse {
    private Long id;
    private String name;
    @JsonProperty("image_url") private String imageUrl;
    @JsonProperty("image_source_url") private String imageSourceUrl;
    @JsonProperty("image_author") private String imageAuthor;
    @JsonProperty("image_license") private String imageLicense;
    @JsonProperty("image_license_version") private String imageLicenseVersion;
    @JsonProperty("short_bio") private String shortBio;
    @JsonProperty("job_tags") private List<String> jobTags;

    /** @param imageUrl S3Service.generateUrl()로 변환을 마친 절대 URL */
    public static CelebrityResponse from(Celebrity celebrity, String imageUrl) {
        ImageLicense license = celebrity.getImageLicense();

        return CelebrityResponse.builder()
                .id(celebrity.getId())
                .name(celebrity.getName())
                .imageUrl(imageUrl)
                .imageSourceUrl(celebrity.getImageSourceUrl())
                .imageAuthor(celebrity.getImageAuthor())
                .imageLicense(license != null ? license.name() : null)
                .imageLicenseVersion(celebrity.getImageLicenseVersion())
                .shortBio(celebrity.getShortBio())
                .jobTags(celebrity.getJobTags().stream()
                        .map(CelebrityTag::getDescription)
                        .collect(Collectors.toList())
                )
                .build();
    }
}
