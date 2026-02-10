package whoreads.backend.domain.dna.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import whoreads.backend.domain.dna.dto.DnaReqDto;
import whoreads.backend.domain.dna.dto.DnaResDto;
import whoreads.backend.domain.dna.enums.TrackCode;
import whoreads.backend.domain.dna.service.DnaService;
import whoreads.backend.global.response.ApiResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/dna")
public class DnaController implements DnaControllerDocs {

    private final DnaService dnaService;

    // Q1 질문 및 보기 조회
    @GetMapping("/questions/root")
    public ApiResponse<DnaResDto.Question> getRootQuestion() {
        DnaResDto.Question rootQuestion = dnaService.getRootQuestion();

        return ApiResponse.success(rootQuestion);
    }

    // Q2~Q5 질문 및 보기
    @GetMapping("/questions")
    public ApiResponse<DnaResDto.TrackQuestion> getTrackQuestions(@RequestParam("trackCode") TrackCode trackCode) {
        DnaResDto.TrackQuestion trackQuestions = dnaService.getTrackQuestions(trackCode);

        return ApiResponse.success(trackQuestions);
    }

    // 최종 결과 계산
    @PostMapping("/results")
    public ApiResponse<DnaResDto.Result> calculateResult(@RequestBody DnaReqDto.Submit request) {
        DnaResDto.Result result = dnaService.calculateResult(request);

        return ApiResponse.success("테스트 결과가 성공적으로 나왔습니다", result);
    }

}
