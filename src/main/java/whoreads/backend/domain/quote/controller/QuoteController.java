package whoreads.backend.domain.quote.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import whoreads.backend.domain.quote.controller.docs.QuoteControllerDocs;
import whoreads.backend.domain.quote.dto.QuoteRequest;
import whoreads.backend.domain.quote.dto.QuoteResponse;
import whoreads.backend.domain.quote.service.QuoteService;

import java.util.List;

@RestController
@RequestMapping("/api/quotes")
@RequiredArgsConstructor
public class QuoteController implements QuoteControllerDocs {

    private final QuoteService quoteService;

    @Override
    @PostMapping
    public ResponseEntity<Void> registerQuote(@RequestBody QuoteRequest request) {
        quoteService.registerQuote(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/books/{bookId}")
    public ResponseEntity<List<QuoteResponse>> getQuotesByBook(@PathVariable Long bookId) {
        return ResponseEntity.ok(quoteService.getQuotesByBook(bookId));
    }

    @GetMapping("/celebrities/{celebrityId}")
    public ResponseEntity<List<QuoteResponse>> getQuotesByCelebrity(@PathVariable Long celebrityId) {
        return ResponseEntity.ok(quoteService.getQuotesByCelebrity(celebrityId));
    }
}