package whoreads.backend.domain.userbook.dto;

import lombok.Getter;
import whoreads.backend.domain.userbook.enums.ReadingStatus;

public class UserBookRequest {

    @Getter
    public static class UpdateStatus {
        private ReadingStatus readingStatus;
        private Integer readingPage;
    }
}
