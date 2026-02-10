package whoreads.backend.domain.dna.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import whoreads.backend.domain.dna.dto.DnaReqDto;
import whoreads.backend.domain.dna.dto.DnaResDto;
import whoreads.backend.domain.dna.enums.TrackCode;
import whoreads.backend.global.response.ApiResponse;

@Tag(name = "DNA Test", description = "사용자의 독서 목적과 성향을 분석하는 독서 DNA Test API")
public interface DnaControllerDocs {

    @Operation(
            summary = "Q1 질문 및 보기 조회",
            description = "Q1 질문과 5가지 보기를 조회합니다."
    )
    ApiResponse<DnaResDto.Question> getRootQuestion();

    @Operation(
            summary = "트랙별 문항 및 보기 조회",
            description = "Q1에서 선택한 보기에 따라 트랙이 나뉩니다. \n\n" +
                    "트랙에 맞는 Q2~Q5 질문과 보기를 조회합니다."
    )
    ApiResponse<DnaResDto.TrackQuestion> getTrackQuestions(@Parameter(
            description = "사용자가 Q1에서 선택한 트랙 코드 \n\n"
                    + "트랙 코드: COMFORT(마음 정리/위로), HABIT(실행력/습관), CAREER(커리어/시야 넓히기), INSIGHT(사고 확장/관점), FOCUS(재미/몰입)")
            @RequestParam("trackCode") TrackCode trackCode
    );

    @Operation(
            summary = "테스트 결과 계산 및 인물 매칭",
            description = "사용자의 답변을 바탕으로 장르 점수를 더해 최종 인물 매칭 결과를 반환합니다.\n\n" +
                    "- **track_code**: 사용자가 Q1에서 선택한 트랙 코드\n\n" +
                    "- **selected_option_ids**: 사용자가 Q2~Q5에서 선택한 보기의 id 값들"
    )
    ApiResponse<DnaResDto.Result> calculateResult(@RequestBody DnaReqDto.Submit request);
}
