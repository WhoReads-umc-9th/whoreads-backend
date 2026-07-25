package whoreads.backend.domain.celebrity.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CelebrityImageRequest {

    @JsonProperty("image_url")
    @NotBlank(message = "이미지 URL은 필수입니다.")
    @URL(message = "올바른 URL 형식이어야 합니다.")
    private String imageUrl;
}