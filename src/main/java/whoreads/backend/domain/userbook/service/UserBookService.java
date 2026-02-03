package whoreads.backend.domain.userbook.service;

import whoreads.backend.domain.userbook.dto.UserBookRequest;
import whoreads.backend.domain.userbook.dto.UserBookResponse;
import whoreads.backend.domain.userbook.enums.ReadingStatus;

public interface UserBookService {

    UserBookResponse.Summary getLibrarySummary();

    UserBookResponse.BookList getBookList(ReadingStatus status, Long cursor, Integer size);

    UserBookResponse.Detail addBookToLibrary(Long bookId);

    UserBookResponse.Detail updateUserBook(Long userBookId, UserBookRequest.UpdateStatus request);
}
