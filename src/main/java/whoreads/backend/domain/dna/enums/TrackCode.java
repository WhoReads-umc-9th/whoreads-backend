package whoreads.backend.domain.dna.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TrackCode {

    COMFORT("마음 정리/위로"),
    HABIT("실행력/습관"),
    CAREER("커리어/시야 넓히기"),
    INSIGHT("사고 확장/관점"),
    FOCUS("재미/몰입");

    private final String description;
}
