package whoreads.backend.domain.quote.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum QuoteSourceType {

    INTERVIEW("인터뷰"),

    VIDEO("유튜브 및 영상 매체"),

    SOCIAL_MEDIA("소셜 미디어"),

    ARTICLE("기사"),

    MAGAZINE("잡지"),

    ETC("기타");

    private final String description;
}