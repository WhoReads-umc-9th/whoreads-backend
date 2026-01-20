package whoreads.backend.domain.book.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import whoreads.backend.domain.book.dto.BookResponse;
import whoreads.backend.domain.book.service.AladinBookService;

import java.util.List;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

    private final AladinBookService aladinBookService;

    @GetMapping
    public List<BookResponse> getAllBooks() {
        return List.of();
    }

    @GetMapping("/search")
    public List<BookResponse> searchBooks(@RequestParam String keyword) {
        return aladinBookService.searchBooks(keyword);
    }
}