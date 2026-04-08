package whoreads.backend.domain.dna.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import whoreads.backend.domain.dna.enums.TrackCode;

import java.util.List;

public class DnaReqDto {

    public record Submit(
            // Q1에서 선택한 독서 목적
            @Schema(example = "COMFORT")
            TrackCode trackCode,

            // Q2~Q5에서 선택한 보기(DnaOption)의 ID 리스트(장르 점수 합산)
            @Size(min = 4, max = 4, message = "Q2부터 Q5까지 총 4개의 답변이 필요합니다.")
            @Schema(example = "[6, 10, 14, 18]")
            List<Long> selectedOptionIds
    ){}
}
