package whoreads.backend.domain.dna.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import whoreads.backend.domain.dna.enums.TrackCode;

import java.util.List;

public class DnaReqDto {

    public record Submit(
            // Q1에서 선택한 독서 목적
            @Schema(example = "COMFORT")
            TrackCode trackCode,

            // Q2~Q5에서 선택한 보기(DnaOption)의 ID 리스트(장르 점수 합산)
            @Schema(example = "[1, 2, 3, 4]")
            List<Long> selectedOptionIds
    ){}
}
