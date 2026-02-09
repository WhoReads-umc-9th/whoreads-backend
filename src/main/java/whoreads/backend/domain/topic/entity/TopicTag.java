package whoreads.backend.domain.topic.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TopicTag {
    LIFE_DIRECTION("삶의 방향"),
    HUMAN_UNDERSTANDING("사람·인간 이해"),
    SOCIETY("사회 이해"),
    MINDSET("사고방식"),
    TURNING_POINT("전환점"),
    TOP_20("최다추천TOP20")
;

    private final String description;
}