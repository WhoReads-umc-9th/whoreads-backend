package whoreads.backend.domain.library.dto;

import tools.jackson.annotation.JsonProperty;
import lombok.Getter;
import whoreads.backend.domain.library.enums.ReadingStatus;

public class UserBookRequest {

    @Getter
    public static class UpdateStatus {
        @JsonProperty("reading_status")
        private ReadingStatus readingStatus;
        @JsonProperty("reading_page")
        private Integer readingPage;
    }
}
