package whoreads.backend.domain.quote.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
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
@Validated // 바꾼 이유: @PathVariable의 제약 조건(@Positive)을 활성화하기 위함
public class QuoteController implements QuoteControllerDocs {

    private final QuoteService quoteService;

    @Override
    @PostMapping
    public ResponseEntity<Void> registerQuote(@RequestBody @Valid QuoteRequest request) { // 바꾼 이유: @Valid 추가하여 DTO 검증 활성화
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
    public ResponseEntity<List<QuoteResponse>> getQuotesByBook(
            @PathVariable @Positive(message = "올바른 책 ID를 입력해주세요.") Long bookId) {
        // 바꾼 이유: 비정상적인 ID 요청을 컨트롤러에서 미리 차단
        return ResponseEntity.ok(quoteService.getQuotesByBook(bookId));
    }

    @Override
    @GetMapping("/celebrities/{celebrityId}")
    public ResponseEntity<List<QuoteResponse>> getQuotesByCelebrity(
            @PathVariable @Positive(message = "올바른 유명인 ID를 입력해주세요.") Long celebrityId) {
        // 바꾼 이유: 비정상적인 ID 요청을 컨트롤러에서 미리 차단
        return ResponseEntity.ok(quoteService.getQuotesByCelebrity(celebrityId));
    }
}