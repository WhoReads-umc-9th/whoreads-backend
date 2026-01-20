package whoreads.backend.domain.book.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Getter
@NoArgsConstructor
@ToString
public class AladinBookResponse {

    @JsonProperty("item")
    private List<AladinItem> items;

    @Getter
    @NoArgsConstructor
    @ToString
    public static class AladinItem {
        private String title;
        private String author;
        private String cover; // 표지
        // private String coverBigUrl; // URL 확인 필요 시 사용
    }
}