package whoreads.backend.domain.book.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import whoreads.backend.domain.book.controller.docs.BookControllerDocs;
import whoreads.backend.domain.book.dto.BookRequest;
import whoreads.backend.domain.book.dto.BookResponse;
import whoreads.backend.domain.book.entity.Book;
import whoreads.backend.domain.book.service.AladinBookService;
import whoreads.backend.domain.book.service.BookService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController implements BookControllerDocs {

    private final AladinBookService aladinBookService;
    private final BookService bookService;

    @Override
    @GetMapping
    public List<BookResponse> getAllBooks(@RequestParam(required = false) String keyword) {
        return bookService.getAllBooks(keyword).stream()
                .map(BookResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @GetMapping("/search")
    public List<BookResponse> aladinSearchBooks(@RequestParam String keyword) {
        return aladinBookService.searchBooks(keyword);
    }

    @Override
    @PostMapping
    public ResponseEntity<BookResponse> registerBook(@RequestBody BookRequest request) {
        Book savedBook = bookService.registerBook(request.toEntity());
        return ResponseEntity.ok(BookResponse.from(savedBook));
    }

    @Override
    @GetMapping("/ranks")
    public ResponseEntity<List<BookResponse>> getMostRecommendedBooks() {
        List<Book> books = bookService.getMostRecommendedBooks(20);

        List<BookResponse> response = books.stream()
                .map(BookResponse::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }
}