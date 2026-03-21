package whoreads.backend.domain.notification.entity;

import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FollowLink {
    private Long bookId;
    private Long celebrityId;
}
