package whoreads.backend.domain.userbook.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import whoreads.backend.domain.userbook.dto.UserBookRequest;
import whoreads.backend.domain.userbook.dto.UserBookResponse;
import whoreads.backend.domain.userbook.enums.ReadingStatus;
import whoreads.backend.domain.userbook.repository.UserBookRepository;
import whoreads.backend.global.exception.CustomException;
import whoreads.backend.global.exception.ErrorCode;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserBookServiceImpl implements UserBookService {

    private final UserBookRepository userBookRepository;

    @Override
    public UserBookResponse.Summary getLibrarySummary() {
        // TODO: 실제 구현 시 로그인한 사용자의 데이터 조회
        return UserBookResponse.Summary.builder()
                .completedNumber(5)
                .readingNumber(2)
                .readTime(12345L)
                .build();
    }

    @Override
    public UserBookResponse.BookList getBookList(ReadingStatus status, Long cursor, Integer size) {
        // TODO: 실제 구현 시 로그인한 사용자의 데이터 + 커서 페이징 적용
        List<UserBookResponse.SimpleBook> mockBooks = List.of(
                UserBookResponse.SimpleBook.builder()
                        .bookId(1L)
                        .bookTitle("클린 코드")
                        .bookAuthor("로버트 C. 마틴")
                        .coverUrl("https://example.com/cover1.jpg")
                        .readingPage(150)
                        .totalPage(464)
                        .build(),
                UserBookResponse.SimpleBook.builder()
                        .bookId(2L)
                        .bookTitle("이펙티브 자바")
                        .bookAuthor("조슈아 블로크")
                        .coverUrl("https://example.com/cover2.jpg")
                        .readingPage(null)
                        .totalPage(412)
                        .build()
        );

        return UserBookResponse.BookList.builder()
                .books(mockBooks)
                .nextCursor(3L)
                .hasNext(true)
                .build();
    }

    @Override
    public UserBookResponse.Detail addBookToLibrary(Long bookId) {
        // TODO: 실제 구현 시
        // 1. 로그인한 사용자 조회
        // 2. bookId로 Book 조회 (없으면 404)
        // 3. 이미 서재에 있는지 확인 (있으면 409)
        // 4. UserBook 생성 (WISH 상태)
        return UserBookResponse.Detail.builder()
                .userBookId(100L)
                .bookId(bookId)
                .bookTitle("새로 추가된 책")
                .bookAuthor("작가명")
                .coverUrl("https://example.com/cover.jpg")
                .readingStatus(ReadingStatus.WISH)
                .readingPage(null)
                .totalPage(300)
                .build();
    }

    @Override
    public UserBookResponse.Detail updateUserBook(Long userBookId, UserBookRequest.UpdateStatus request) {
        // 비즈니스 로직: status가 READING이 아닌데 readingPage가 있으면 에러
        if (request.getReadingStatus() != ReadingStatus.READING && request.getReadingPage() != null) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }

        // TODO: 실제 구현 시
        // 1. userBookId로 UserBook 조회 (없으면 404)
        // 2. 로그인한 사용자의 책인지 확인
        // 3. status 변경
        // 4. status가 READING이면 readingPage도 변경 (null이 아닐 때만)
        Integer readingPage = request.getReadingStatus() == ReadingStatus.READING
                ? (request.getReadingPage() != null ? request.getReadingPage() : 150)
                : 150; // 기존 값 유지 (mock)

        return UserBookResponse.Detail.builder()
                .userBookId(userBookId)
                .bookId(1L)
                .bookTitle("수정된 책")
                .bookAuthor("작가명")
                .coverUrl("https://example.com/cover.jpg")
                .readingStatus(request.getReadingStatus())
                .readingPage(readingPage)
                .totalPage(300)
                .build();
    }
}
