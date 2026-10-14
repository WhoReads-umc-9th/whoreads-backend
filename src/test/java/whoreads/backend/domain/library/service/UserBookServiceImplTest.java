package whoreads.backend.domain.library.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import whoreads.backend.domain.book.entity.Book;
import whoreads.backend.domain.library.dto.UserBookRequest;
import whoreads.backend.domain.library.entity.UserBook;
import whoreads.backend.domain.library.enums.ReadingStatus;
import whoreads.backend.domain.library.repository.UserBookRepository;
import whoreads.backend.domain.member.entity.Member;
import whoreads.backend.global.exception.CustomException;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class UserBookServiceImplTest {

    private static final Long MEMBER_ID = 1L;
    private static final Long USER_BOOK_ID = 10L;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().findAndRegisterModules();

    @Mock
    private UserBookRepository userBookRepository;

    @InjectMocks
    private UserBookServiceImpl userBookService;

    private UserBook givenUserBook(ReadingStatus status) {
        Member member = Member.builder().id(MEMBER_ID).build();
        Book book = Book.builder().title("1984").totalPage(300).build();
        UserBook userBook = UserBook.builder().readingStatus(status).member(member).book(book).build();
        given(userBookRepository.findById(USER_BOOK_ID)).willReturn(Optional.of(userBook));
        return userBook;
    }

    private UserBookRequest.UpdateReadingInfo request(String json) throws Exception {
        return OBJECT_MAPPER.readValue(json, UserBookRequest.UpdateReadingInfo.class);
    }

    @Test
    @DisplayName("상태만 보내면 상태만 바뀌고 날짜는 자동으로 채우지 않는다")
    void statusOnly_doesNotSetDates() throws Exception {
        UserBook userBook = givenUserBook(ReadingStatus.WISH);

        userBookService.updateUserBook(MEMBER_ID, USER_BOOK_ID, request("{\"reading_status\":\"READING\"}"));

        assertThat(userBook.getReadingStatus()).isEqualTo(ReadingStatus.READING);
        assertThat(userBook.getStartedAt()).isNull();
        assertThat(userBook.getCompletedAt()).isNull();
    }

    @Test
    @DisplayName("READING에서 시작일, 완료일, 페이지를 입력한 그대로 저장한다")
    void reading_savesDatesAndPage() throws Exception {
        UserBook userBook = givenUserBook(ReadingStatus.READING);

        userBookService.updateUserBook(MEMBER_ID, USER_BOOK_ID, request(
                "{\"started_at\":\"2026-09-01\",\"completed_at\":\"2026-09-20\",\"reading_page\":120}"));

        assertThat(userBook.getReadingStatus()).isEqualTo(ReadingStatus.READING);
        assertThat(userBook.getStartedAt()).isEqualTo(LocalDate.of(2026, 9, 1));
        assertThat(userBook.getCompletedAt()).isEqualTo(LocalDate.of(2026, 9, 20));
        assertThat(userBook.getReadingPage()).isEqualTo(120);
    }

    @Test
    @DisplayName("COMPLETE로 바꿔도 페이지와 날짜는 기존 값 그대로 유지한다")
    void toComplete_keepsPageAndDates() throws Exception {
        UserBook userBook = givenUserBook(ReadingStatus.READING);
        userBook.updateStartedAt(LocalDate.of(2026, 9, 1));
        userBook.updateReadingPage(120);

        userBookService.updateUserBook(MEMBER_ID, USER_BOOK_ID, request("{\"reading_status\":\"COMPLETE\"}"));

        assertThat(userBook.getReadingStatus()).isEqualTo(ReadingStatus.COMPLETE);
        assertThat(userBook.getStartedAt()).isEqualTo(LocalDate.of(2026, 9, 1));
        assertThat(userBook.getCompletedAt()).isNull();
        assertThat(userBook.getReadingPage()).isEqualTo(120);
    }

    @Test
    @DisplayName("COMPLETE에서 READING으로 되돌려도 날짜와 페이지는 유지된다")
    void completeToReading_keepsInfo() throws Exception {
        UserBook userBook = givenUserBook(ReadingStatus.COMPLETE);
        userBook.updateStartedAt(LocalDate.of(2026, 9, 1));
        userBook.updateCompletedAt(LocalDate.of(2026, 9, 20));
        userBook.updateReadingPage(120);

        userBookService.updateUserBook(MEMBER_ID, USER_BOOK_ID, request("{\"reading_status\":\"READING\"}"));

        assertThat(userBook.getStartedAt()).isEqualTo(LocalDate.of(2026, 9, 1));
        assertThat(userBook.getCompletedAt()).isEqualTo(LocalDate.of(2026, 9, 20));
        assertThat(userBook.getReadingPage()).isEqualTo(120);
    }

    @Test
    @DisplayName("COMPLETE에서 시작일, 완료일을 수정할 수 있다")
    void complete_updatesDates() throws Exception {
        UserBook userBook = givenUserBook(ReadingStatus.COMPLETE);

        userBookService.updateUserBook(MEMBER_ID, USER_BOOK_ID, request(
                "{\"started_at\":\"2026-09-01\",\"completed_at\":\"2026-09-20\"}"));

        assertThat(userBook.getStartedAt()).isEqualTo(LocalDate.of(2026, 9, 1));
        assertThat(userBook.getCompletedAt()).isEqualTo(LocalDate.of(2026, 9, 20));
    }

    @Test
    @DisplayName("COMPLETE에서 페이지를 보내면 400")
    void pageOnComplete_throws() throws Exception {
        givenUserBook(ReadingStatus.COMPLETE);

        assertThatThrownBy(() -> userBookService.updateUserBook(MEMBER_ID, USER_BOOK_ID,
                request("{\"reading_page\":300}")))
                .isInstanceOf(CustomException.class);
    }

    @Test
    @DisplayName("WISH에서 날짜나 페이지를 보내면 400")
    void infoOnWish_throws() throws Exception {
        givenUserBook(ReadingStatus.WISH);

        assertThatThrownBy(() -> userBookService.updateUserBook(MEMBER_ID, USER_BOOK_ID,
                request("{\"started_at\":\"2026-09-01\"}")))
                .isInstanceOf(CustomException.class);
        assertThatThrownBy(() -> userBookService.updateUserBook(MEMBER_ID, USER_BOOK_ID,
                request("{\"reading_page\":10}")))
                .isInstanceOf(CustomException.class);
    }

    @Test
    @DisplayName("READING에서 저장된 완료일보다 늦은 시작일만 보내면 시작일을 반영하고 완료일은 비운다")
    void reading_laterStartedAt_clearsCompletedAt() throws Exception {
        UserBook userBook = givenUserBook(ReadingStatus.COMPLETE);
        userBook.updateStartedAt(LocalDate.of(2026, 9, 1));
        userBook.updateCompletedAt(LocalDate.of(2026, 9, 20));

        userBookService.updateUserBook(MEMBER_ID, USER_BOOK_ID, request(
                "{\"reading_status\":\"READING\",\"started_at\":\"2026-09-25\"}"));

        assertThat(userBook.getReadingStatus()).isEqualTo(ReadingStatus.READING);
        assertThat(userBook.getStartedAt()).isEqualTo(LocalDate.of(2026, 9, 25));
        assertThat(userBook.getCompletedAt()).isNull();
    }

    @Test
    @DisplayName("COMPLETE에서 저장된 완료일보다 늦은 시작일을 보내면 400")
    void complete_laterStartedAt_throws() throws Exception {
        UserBook userBook = givenUserBook(ReadingStatus.COMPLETE);
        userBook.updateCompletedAt(LocalDate.of(2026, 9, 20));

        assertThatThrownBy(() -> userBookService.updateUserBook(MEMBER_ID, USER_BOOK_ID,
                request("{\"started_at\":\"2026-09-25\"}")))
                .isInstanceOf(CustomException.class);
        assertThat(userBook.getCompletedAt()).isEqualTo(LocalDate.of(2026, 9, 20));
    }

    @Test
    @DisplayName("시작일이 완료일보다 늦으면 400")
    void startedAfterCompleted_throws() throws Exception {
        givenUserBook(ReadingStatus.COMPLETE);

        assertThatThrownBy(() -> userBookService.updateUserBook(MEMBER_ID, USER_BOOK_ID,
                request("{\"started_at\":\"2026-09-20\",\"completed_at\":\"2026-09-01\"}")))
                .isInstanceOf(CustomException.class);
    }

    @Test
    @DisplayName("미래 날짜나 전체 페이지 초과는 400")
    void futureDateOrPageOverflow_throws() throws Exception {
        givenUserBook(ReadingStatus.READING);
        String tomorrow = LocalDate.now().plusDays(1).toString();

        assertThatThrownBy(() -> userBookService.updateUserBook(MEMBER_ID, USER_BOOK_ID,
                request("{\"started_at\":\"" + tomorrow + "\"}")))
                .isInstanceOf(CustomException.class);
        assertThatThrownBy(() -> userBookService.updateUserBook(MEMBER_ID, USER_BOOK_ID,
                request("{\"reading_page\":301}")))
                .isInstanceOf(CustomException.class);
    }
}
