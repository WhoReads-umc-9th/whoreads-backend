package whoreads.backend.domain.userbook.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import whoreads.backend.domain.userbook.controller.docs.UserBookControllerDocs;
import whoreads.backend.domain.userbook.dto.UserBookRequest;
import whoreads.backend.domain.userbook.dto.UserBookResponse;
import whoreads.backend.domain.userbook.enums.ReadingStatus;
import whoreads.backend.domain.userbook.service.UserBookService;
import whoreads.backend.global.response.ApiResponse;

@RestController
@RequestMapping("/api/me/library")
@RequiredArgsConstructor
public class UserBookController implements UserBookControllerDocs {

    private final UserBookService userBookService;

    @Override
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<UserBookResponse.Summary>> getLibrarySummary() {
        UserBookResponse.Summary summary = userBookService.getLibrarySummary();
        return ResponseEntity.ok(ApiResponse.success(summary));
    }

    @Override
    @GetMapping("/list")
    public ResponseEntity<ApiResponse<UserBookResponse.BookList>> getBookList(
            @RequestParam ReadingStatus status,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        UserBookResponse.BookList bookList = userBookService.getBookList(status, cursor, size);
        return ResponseEntity.ok(ApiResponse.success(bookList));
    }

    @Override
    @PostMapping("/book/{bookId}")
    public ResponseEntity<ApiResponse<UserBookResponse.Detail>> addBookToLibrary(
            @PathVariable Long bookId
    ) {
        UserBookResponse.Detail detail = userBookService.addBookToLibrary(bookId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(detail));
    }

    @Override
    @PatchMapping("/book/{userBookId}")
    public ResponseEntity<ApiResponse<UserBookResponse.Detail>> updateUserBook(
            @PathVariable Long userBookId,
            @RequestBody UserBookRequest.UpdateStatus request
    ) {
        UserBookResponse.Detail detail = userBookService.updateUserBook(userBookId, request);
        return ResponseEntity.ok(ApiResponse.success(detail));
    }
}
