package whoreads.backend.domain.quote.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import whoreads.backend.domain.quote.controller.docs.QuoteControllerDocs;
import whoreads.backend.domain.quote.dto.QuoteRequest;
import whoreads.backend.domain.quote.dto.QuoteResponse;
import whoreads.backend.domain.quote.service.QuoteService;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/quotes")
@RequiredArgsConstructor
public class QuoteController implements QuoteControllerDocs {

    private final QuoteService quoteService;

    @Override
    @PostMapping
    public ResponseEntity<Void> registerQuote(@RequestBody @Valid QuoteRequest request) { // @Valid 추가
        Long createdId = quoteService.registerQuote(request);

        // 생성된 리소스의 URI 생성 (예: /api/quotes/1) - 상세 조회 API가 있다면 유용
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdId)
                .toUri();

        return ResponseEntity.created(location).build(); // 201 Created 반환
    }

    @Override
    @GetMapping("/books/{bookId}")
    public ResponseEntity<List<QuoteResponse>> getQuotesByBook(@PathVariable Long bookId) {
        return ResponseEntity.ok(quoteService.getQuotesByBook(bookId));
    }

    @Override
    @GetMapping("/celebrities/{celebrityId}")
    public ResponseEntity<List<QuoteResponse>> getQuotesByCelebrity(@PathVariable Long celebrityId) {
        return ResponseEntity.ok(quoteService.getQuotesByCelebrity(celebrityId));
    }
}