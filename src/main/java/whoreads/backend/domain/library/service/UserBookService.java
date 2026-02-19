package whoreads.backend.domain.library.service;

import whoreads.backend.domain.library.dto.UserBookRequest;
import whoreads.backend.domain.library.dto.UserBookResponse;
import whoreads.backend.domain.library.enums.ReadingStatus;

public interface UserBookService {

    UserBookResponse.Summary getLibrarySummary(Long memberId);

    UserBookResponse.BookList getBookList(Long memberId, ReadingStatus status, Long cursor, Integer size);

    UserBookResponse.AddResult addBookToLibrary(Long memberId, Long bookId);

    void updateUserBook(Long memberId, Long userBookId, UserBookRequest.UpdateStatus request);

    void deleteBookFromLibrary(Long memberId, Long userBookId);
}
