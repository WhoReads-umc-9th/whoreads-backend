package whoreads.backend.domain.book.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import whoreads.backend.domain.book.controller.docs.BookControllerDocs;
import whoreads.backend.domain.book.dto.BookDetailResponse;
import whoreads.backend.domain.book.dto.BookRequest;
import whoreads.backend.domain.book.dto.BookResponse;
import whoreads.backend.domain.book.entity.Book;
import whoreads.backend.domain.book.service.AladinBookService;
import whoreads.backend.domain.book.service.BookService;
import whoreads.backend.domain.topic.entity.TopicTag;
import whoreads.backend.global.response.ApiResponse;
import whoreads.backend.global.response.PageResponse;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
@Validated // 바꾼 이유: @RequestParam, @PathVariable에 설정한 제약조건을 활성화
public class BookController implements BookControllerDocs {

    private final AladinBookService aladinBookService;
    private final BookService bookService;

    @Override
    @GetMapping
    public PageResponse<BookResponse> getAllBooks(
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 20) Pageable pageable) {
        // 바꾼 이유: 전체 도서를 한 번에 내려주느라 목록 API가 느렸음 → 페이징 적용
        return PageResponse.of(bookService.getAllBooks(keyword, pageable), BookResponse::from);
    }

    @Override
    @GetMapping("/aladin")
    public List<BookResponse> aladinSearchBooks(
            @RequestParam @NotBlank(message = "알라딘 검색어는 필수입니다.") String keyword) { // 바꾼 이유: 빈 문자열 요청 차단
        return aladinBookService.searchBooks(keyword);
    }

    @Override
    @PostMapping
    public ResponseEntity<BookResponse> registerBook(@RequestBody @Valid BookRequest request) { // 바꾼 이유: @Valid 추가
        Book book = bookService.registerBook(request);
        return ResponseEntity.ok(BookResponse.from(book));
    }

    // 정리: /api/topics/TOP_20/books 와 동일한 결과. 신규 연동은 /api/topics/{theme}/books 사용 권장
    @Override
    @Deprecated
    @GetMapping("/most-recommended")
    public ResponseEntity<List<BookResponse>> getMostRecommendedBooks(
            @RequestParam(defaultValue = "20") @Positive(message = "가져올 개수는 1 이상이어야 합니다.") int limit) { // 바꾼 이유: limit에 음수나 0이 들어오면 에러가 나므로 @Positive 추가
        List<Book> books = bookService.getMostRecommendedBooks(limit);
        List<BookResponse> response = books.stream()
                .map(BookResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @Override
    @GetMapping("/{bookId}/detail")
    public ApiResponse<BookDetailResponse> getBookDetail(
            @PathVariable @Positive(message = "올바른 책 ID를 입력해주세요.") Long bookId, // 바꾼 이유: ID값 검증
            @AuthenticationPrincipal Long memberId) {
        // 바꾼 이유: 기존 resolveCurrentMemberId()는 principal을 CustomUserDetails로 캐스팅했지만
        // JwtAuthenticationFilter가 principal에 memberId(Long)를 넣으므로 instanceof가 절대 매칭되지 않아
        // 로그인 상태에서도 memberId가 항상 null → reading_info가 내려가지 않았음.
        // 다른 컨트롤러들과 동일하게 @AuthenticationPrincipal Long로 통일 (비로그인 시 null)
        return ApiResponse.success(bookService.getBookDetail(bookId, memberId));
    }

    // 정리: /api/topics/{theme}/books 와 동일한 결과. 신규 연동은 /api/topics/{theme}/books 사용 권장
    @Override
    @Deprecated
    @GetMapping("/themes/{theme}")
    public ResponseEntity<List<BookResponse>> getBooksByTheme(
            @PathVariable TopicTag theme,
            @RequestParam(defaultValue = "20") @Positive int limit // 바꾼 이유: 음수 리밋 방지
    ) {
        List<Book> books = bookService.getBooksByTheme(theme, limit);
        List<BookResponse> response = books.stream()
                .map(BookResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }
}