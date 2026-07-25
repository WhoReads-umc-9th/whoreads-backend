package whoreads.backend.domain.celebrity.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CelebrityImageResponse {
    private Long id;
    @JsonProperty("image_url")
    private String imageUrl;

    public static CelebrityImageResponse from(Long id, String imageUrl) {
        return new CelebrityImageResponse(id, imageUrl);
    }
}