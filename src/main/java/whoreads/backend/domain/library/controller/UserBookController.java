package whoreads.backend.domain.library.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import whoreads.backend.domain.library.controller.docs.UserBookControllerDocs;
import whoreads.backend.domain.library.dto.UserBookRequest;
import whoreads.backend.domain.library.dto.UserBookResponse;
import whoreads.backend.domain.library.enums.ReadingStatus;
import whoreads.backend.domain.library.service.UserBookService;
import whoreads.backend.global.response.ApiResponse;

@RestController
@RequestMapping("/api/me/library")
@RequiredArgsConstructor
public class UserBookController implements UserBookControllerDocs {

    private final UserBookService userBookService;

    @Override
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<UserBookResponse.Summary>> getLibrarySummary(
            @AuthenticationPrincipal Long memberId
    ) {
        UserBookResponse.Summary summary = userBookService.getLibrarySummary(memberId);
        return ResponseEntity.ok(ApiResponse.success(summary));
    }

    @Override
    @GetMapping("/list")
    public ResponseEntity<ApiResponse<UserBookResponse.BookList>> getBookList(
            @AuthenticationPrincipal Long memberId,
            @RequestParam ReadingStatus status,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        UserBookResponse.BookList bookList = userBookService.getBookList(memberId, status, cursor, size);
        return ResponseEntity.ok(ApiResponse.success(bookList));
    }

    @Override
    @PostMapping("/book/{bookId}")
    public ResponseEntity<ApiResponse<UserBookResponse.AddResult>> addBookToLibrary(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long bookId
    ) {
        UserBookResponse.AddResult result = userBookService.addBookToLibrary(memberId, bookId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("책을 추가했습니다.", result));
    }

    @Override
    @PatchMapping("/book/{userBookId}")
    public ResponseEntity<ApiResponse<Void>> updateUserBook(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long userBookId,
            @RequestBody UserBookRequest.UpdateStatus request
    ) {
        userBookService.updateUserBook(memberId, userBookId, request);
        return ResponseEntity.ok(ApiResponse.success("책 상태를 변경했습니다."));
    }

    @Override
    @DeleteMapping("/book/{userBookId}")
    public ResponseEntity<ApiResponse<Void>> deleteBookFromLibrary(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long userBookId
    ) {
        userBookService.deleteBookFromLibrary(memberId, userBookId);
        return ResponseEntity.ok(ApiResponse.success("서재에서 책이 삭제되었습니다."));
    }
}
