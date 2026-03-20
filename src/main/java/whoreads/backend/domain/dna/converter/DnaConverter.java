package whoreads.backend.domain.dna.converter;

import org.springframework.stereotype.Component;
import whoreads.backend.domain.celebrity.entity.Celebrity;
import whoreads.backend.domain.celebrity.entity.CelebrityTag;
import whoreads.backend.domain.dna.dto.DnaResDto;
import whoreads.backend.domain.dna.entity.DnaOption;
import whoreads.backend.domain.dna.entity.DnaQuestion;
import whoreads.backend.domain.dna.enums.TrackCode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class DnaConverter {

    // DnaQuestion entity and DnaOption list -> DnaResDto.Question
    public static DnaResDto.Question toQuestionDto(DnaQuestion question, List<DnaOption> options) {
        List<DnaResDto.Option> optionDtos = new ArrayList<>();
        for (DnaOption option: options)
            optionDtos.add(toOptionDto(option));

        return new DnaResDto.Question(
                question.getId(),
                question.getStep(),
                question.getContent(),
                optionDtos
        );
    }


    // DnaOption 엔티티 -> DnaResDto.Option
    public static DnaResDto.Option toOptionDto(DnaOption option) {
        return new DnaResDto.Option(
                option.getId(),
                option.getContent(),
                option.getTrack().getTrackCode()
        );
    }


    public static DnaResDto.Result toResultDto(Celebrity celebrity, TrackCode trackCode, String description) {
        String headLine = switch (trackCode) {
            case COMFORT -> "있는 그대로의 마음을 이해하기 위해";
            case HABIT -> "행동을 바꾸기 위해";
            case CAREER -> "더 넓은 세상에서 커리어를 탐색하기 위해";
            case INSIGHT -> "세상을 다른 관점으로 바라보기 위해";
            case FOCUS -> "읽는 재미 그 자체를 느끼기 위해";
            default -> "당신만을 위한 독서 DNA 결과";
        };

        // description 문장 단위로 파싱
        String[] parts = description.split("\\.");
        List<String> descriptionList = new ArrayList<>();
        for (String s: parts) {
            String str = s.trim();
            if (!str.isEmpty())
                descriptionList.add(str + ".");
        }

        List<String> jobTags = new ArrayList<>();
        for (CelebrityTag tag: celebrity.getJobTags())
            jobTags.add(tag.getDescription());

        return DnaResDto.Result.builder()
                .resultHeaLine(String.format("지금 당신은 '%s'를 위해 독서를 하는 사람입니다.", headLine))
                .description(descriptionList)
                .celebrityId(celebrity.getId())
                .celebrityName(celebrity.getName())
                .imageUrl(celebrity.getImageUrl())
                .jobTags(jobTags)
                .build();
    }
}