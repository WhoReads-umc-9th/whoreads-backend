package whoreads.backend.domain.library.service;

import whoreads.backend.domain.library.dto.UserBookRequest;
import whoreads.backend.domain.library.dto.UserBookResponse;
import whoreads.backend.domain.library.enums.ReadingStatus;

public interface UserBookService {

    UserBookResponse.Summary getLibrarySummary();

    UserBookResponse.BookList getBookList(ReadingStatus status, Long cursor, Integer size);

    UserBookResponse.AddResult addBookToLibrary(Long bookId);

    void updateUserBook(Long userBookId, UserBookRequest.UpdateStatus request);

    void deleteBookFromLibrary(Long userBookId);
}
