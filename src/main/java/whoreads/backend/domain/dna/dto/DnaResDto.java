package whoreads.backend.domain.dna.dto;

import tools.jackson.annotation.JsonProperty;
import lombok.Builder;
import whoreads.backend.domain.dna.enums.TrackCode;

import java.util.List;

public class DnaResDto {

    @Builder
    public record Question(
            Long id,
            int step,
            String content,
            List<Option> options
    ){}

    @Builder
    public record Option(
            Long id,
            String content,
            @JsonProperty("track_code") TrackCode trackCode
    ){}

    @Builder
    public record TrackQuestion(
            @JsonProperty("track_code") TrackCode trackCode,
            List<Question> questions
    ){}

    @Builder
    public record TrackOptions(
            @JsonProperty("question_id") Long questionId,
            List<Option> options
    ){}

    @Builder
    public record Result(
            @JsonProperty("result_headline") String resultHeadLine,
            List<String> description,
            @JsonProperty("celebrity_id") Long celebrityId,
            @JsonProperty("celebrity_name") String celebrityName,
            @JsonProperty("image_url") String imageUrl,
            @JsonProperty("job_tags") List<String> jobTags
    ){}
}
