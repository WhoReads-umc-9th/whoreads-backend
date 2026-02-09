package whoreads.backend.domain.dna.dto;

import lombok.Builder;
import whoreads.backend.domain.dna.enums.TrackCode;

import java.util.List;

public class DnaResDto {

    // Q1 질문 및 보기
    @Builder
    public record Question(
            Long id,
            int step,
            String content,
            List<Option> options
            // Q1 질문 예시
            // content: "사용자는 독서를 통해 어떤 종류의 도움을 가장 필요로 하는가?"
            // options: "마음정리/위로", "실행력/습관", ...
    ){}

    // Q1 ~ Q5 보기
    @Builder
    public record Option(
            Long id,
            String content,
            // 보기를 선택했을 때 진입하게 될 트랙 식별자
            /*
                COMFORT("마음 정리/위로"),
                HABIT("실행력/습관"),
                CAREER("커리어/시야 넓히기"),
                INSIGHT("사고 확장/관점"),
                FOCUS("재미/몰입");
             */
            TrackCode trackCode
    ){}


    // 첫 테스트에서 확정된 트랙에 대한 "질문"
    @Builder
    public record TrackQuestion(
            TrackCode trackCode,
            // 해당 트랙의 Q2~Q5
            // 여기 안에는 보기도 포함되어 있다.
            // 변수명 바꿔야 할듯
            List<Question> questions
    ){}

    // 첫 테스트에서 확정된 트랙에 대한 "보기"
    @Builder
    public record TrackOptions(
            Long questionId,
            List<Option> options
    ){}

    // 테스트 최종 결과 전송
    @Builder
    public record Result(
            String resultHeaLine,  // "지금 당신은 000을 위해 독서를 하는 사람입니다."
            String description,
            String celebrityName,
            String imageUrl

            // 추가해야할 것
            // 000의 추천 도서 확인하기
            // 눌렀을 때 해당 인물 탭으로 이동하는데
            // 인물 도메인에서 만든거 여기로 가져오면 될듯
    ){}
}
