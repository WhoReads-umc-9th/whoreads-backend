package whoreads.backend.domain.library.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import whoreads.backend.domain.library.enums.ReadingStatus;

import java.time.LocalDate;

public class UserBookRequest {

    /**
     * 서재 책 읽기 정보 부분 수정 요청.
     * 모든 필드는 선택값이며, null인 필드는 기존 값을 유지한다.
     */
    @Getter
    public static class UpdateReadingInfo {
        @JsonProperty("reading_status")
        private ReadingStatus readingStatus;
        @JsonProperty("reading_page")
        private Integer readingPage;
        @JsonProperty("started_at")
        private LocalDate startedAt;
        @JsonProperty("completed_at")
        private LocalDate completedAt;
    }
}
