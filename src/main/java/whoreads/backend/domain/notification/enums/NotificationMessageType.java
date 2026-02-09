package whoreads.backend.domain.notification.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import whoreads.backend.domain.notification.event.NotificationEvent;
import whoreads.backend.global.exception.CustomException;
import whoreads.backend.global.exception.ErrorCode;

import java.util.concurrent.ThreadLocalRandom;


@Getter
@RequiredArgsConstructor
public enum NotificationMessageType {
    NEW_FOLLOW_BOOK("%s의 서재에 새 책이 추가됐어요!", "『%s』 %s") {
        @Override
        public String[] generate(NotificationEvent event) {
            if (event instanceof NotificationEvent.FollowEvent e) {
                return new String[]{
                        String.format(getTitleTemplate(), e.celebName()),
                        String.format(getBodyTemplate(), e.bookName(), e.authorName())
                };
            }
            throw new CustomException(ErrorCode.FCM_SEND_FAILED);
        }
    },

    BOOK_ROUTINE("독서 루틴 알림", "") {
        private final String[][] messages = {
                {"미리 정해 둔 독서 시간이에요", "오늘도 부담 없이 시작해 보세요"},
                {"설정해 둔 독서 시간이 되었어요", "잠깐이라도 책을 펼쳐보세요."},
                {"설정한 독서 루틴 시간이에요.", "오늘의 독서를 가볍게 시작해 보세요."}
        };

        @Override
        public String[] generate(NotificationEvent event) {
            int randomIndex = ThreadLocalRandom.current().nextInt(messages.length);
            return new String[]{messages[randomIndex][0], messages[randomIndex][1]};
        }
    };

    private final String titleTemplate;
    private final String bodyTemplate;

    public abstract String[] generate(NotificationEvent event);
}