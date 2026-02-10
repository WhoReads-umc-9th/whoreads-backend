package whoreads.backend.domain.quote.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum QuoteSourceType {

    INTERVIEW("인터뷰"),

    YOUTUBE_VIDEO("유튜브 영상"),

    SNS("SNS"),

    ARTICLE("기사"),

    MAGAZINE("매거진");

    private final String description;
}