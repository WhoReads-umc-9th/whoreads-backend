package whoreads.backend.domain.dna.converter;

import org.springframework.stereotype.Component;
import whoreads.backend.domain.celebrity.entity.Celebrity;
import whoreads.backend.domain.dna.dto.DnaResDto;
import whoreads.backend.domain.dna.entity.DnaOption;
import whoreads.backend.domain.dna.entity.DnaQuestion;
import whoreads.backend.domain.dna.enums.TrackCode;

import java.util.ArrayList;
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

        return null;
    }
}