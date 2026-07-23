package whoreads.backend.domain.celebrity.dto;

import lombok.Builder;
import lombok.Getter;
import whoreads.backend.domain.celebrity.entity.CelebrityTag;

@Getter
@Builder
public class CelebrityCategoryResponse {
    private String code; // 필터 파라미터로 쓰는 enum 이름 (예: "SINGER")
    private String name; // 화면 노출용 한글 명칭 (예: "가수")

    public static CelebrityCategoryResponse from(CelebrityTag tag) {
        return CelebrityCategoryResponse.builder()
                .code(tag.name())
                .name(tag.getDescription())
                .build();
    }
}