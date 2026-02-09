package whoreads.backend.domain.library.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import whoreads.backend.domain.book.dto.BookResponse;
import whoreads.backend.domain.library.dto.UserBookRequest;
import whoreads.backend.domain.library.dto.UserBookResponse;
import whoreads.backend.domain.library.enums.ReadingStatus;
import whoreads.backend.domain.library.repository.UserBookRepository;
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
                .completedCount(5)
                .readingCount(2)
                .totalReadMinutes(12345L)
                .build();
    }

    @Override
    public UserBookResponse.BookList getBookList(ReadingStatus status, Long cursor, Integer size) {
        // TODO: 실제 구현 시 로그인한 사용자의 데이터 + 커서 페이징 적용
        List<UserBookResponse.SimpleBook> mockBooks = List.of(
                UserBookResponse.SimpleBook.builder()
                        .book(BookResponse.builder()
                                .id(1L)
                                .title("클린 코드")
                                .authorName("로버트 C. 마틴")
                                .coverUrl("https://image.aladin.co.kr/product/cover1.jpg")
                                .totalPage(464)
                                .build())
                        .readingPage(150)
                        .celebritiesCount(2)
                        .celebrities(List.of(
                                UserBookResponse.CelebritySummary.builder().id(1L).profileUrl("https://example.com/profile1.jpg").build(),
                                UserBookResponse.CelebritySummary.builder().id(2L).profileUrl("https://example.com/profile2.jpg").build()
                        ))
                        .build(),
                UserBookResponse.SimpleBook.builder()
                        .book(BookResponse.builder()
                                .id(2L)
                                .title("이펙티브 자바")
                                .authorName("조슈아 블로크")
                                .coverUrl("https://image.aladin.co.kr/product/cover2.jpg")
                                .totalPage(412)
                                .build())
                        .readingPage(null)
                        .celebritiesCount(0)
                        .celebrities(List.of())
                        .build()
        );

        return UserBookResponse.BookList.builder()
                .books(mockBooks)
                .nextCursor(3L)
                .hasNext(true)
                .build();
    }

    @Override
    public UserBookResponse.AddResult addBookToLibrary(Long bookId) {
        // TODO: 실제 구현 시
        // 1. 로그인한 사용자 조회
        // 2. bookId로 Book 조회 (없으면 404)
        // 3. 이미 서재에 있는지 확인 (있으면 409)
        // 4. UserBook 생성 (WISH 상태)
        return UserBookResponse.AddResult.builder()
                .userBookId(42L)
                .build();
    }

    @Override
    public void updateUserBook(Long userBookId, UserBookRequest.UpdateStatus request) {
        // 비즈니스 로직: status가 READING이 아닌데 readingPage가 있으면 에러
        if (request.getReadingStatus() != ReadingStatus.READING && request.getReadingPage() != null) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "READING 상태인 책만 reading_page를 변경할 수 있습니다.");
        }

        // TODO: 실제 구현 시
        // 1. userBookId로 UserBook 조회 (없으면 404)
        // 2. 로그인한 사용자의 책인지 확인
        // 3. status 변경
        // 4. status가 READING이면 readingPage도 변경 (null이 아닐 때만)
        // 5. reading_page <= totalPage 검증 로직 추가
    }

    @Override
    public void deleteBookFromLibrary(Long userBookId) {
        // TODO: 실제 구현 시
        // 1. userBookId로 UserBook 조회 (없으면 404)
        // 2. 로그인한 사용자의 책인지 확인
        // 3. UserBook 삭제
    }
}
