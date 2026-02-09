package whoreads.backend.domain.library.dto;

import lombok.Getter;
import whoreads.backend.domain.library.enums.ReadingStatus;

public class UserBookRequest {

    @Getter
    public static class UpdateStatus {
        private ReadingStatus readingStatus;
        private Integer readingPage;
    }
}
