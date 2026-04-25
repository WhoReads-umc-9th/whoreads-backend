package whoreads.backend.domain.dna.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import whoreads.backend.domain.dna.enums.TrackCode;

import java.util.List;

public class DnaReqDto {

    public record Submit(
            @JsonProperty("track_code")
            @Schema(example = "COMFORT")
            TrackCode trackCode,

            @JsonProperty("selected_option_ids")
            @Size(min = 4, max = 4, message = "Q2부터 Q5까지 총 4개의 답변이 필요합니다.")
            @Schema(example = "[6, 10, 14, 18]")
            List<Long> selectedOptionIds
    ){}
}
