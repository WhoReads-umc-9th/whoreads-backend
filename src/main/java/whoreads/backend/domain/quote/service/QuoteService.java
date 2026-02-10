package whoreads.backend.domain.quote.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import whoreads.backend.domain.book.entity.Book;
import whoreads.backend.domain.book.entity.BookQuote;
import whoreads.backend.domain.book.repository.BookQuoteRepository;
import whoreads.backend.domain.book.repository.BookRepository;
import whoreads.backend.domain.celebrity.entity.Celebrity;
import whoreads.backend.domain.celebrity.repository.CelebrityRepository;
import whoreads.backend.domain.notification.event.NotificationEvent;
import whoreads.backend.domain.quote.dto.QuoteRequest;
import whoreads.backend.domain.quote.dto.QuoteResponse;
import whoreads.backend.domain.quote.entity.Quote;
import whoreads.backend.domain.quote.entity.QuoteContext;
import whoreads.backend.domain.quote.entity.QuoteSource;
import whoreads.backend.domain.quote.repository.QuoteContextRepository;
import whoreads.backend.domain.quote.repository.QuoteRepository;
import whoreads.backend.domain.quote.repository.QuoteSourceRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuoteService {

    private final QuoteRepository quoteRepository;
    private final BookRepository bookRepository;
    private final CelebrityRepository celebrityRepository;
    private final BookQuoteRepository bookQuoteRepository;
    private final QuoteContextRepository quoteContextRepository;
    private final QuoteSourceRepository quoteSourceRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional
    public Long registerQuote(QuoteRequest request) { // Void -> Long (ID 반환)

        Book book = bookRepository.findById(request.getBookId())
                .orElseThrow(() -> new EntityNotFoundException("책이 없습니다. ID=" + request.getBookId()));
        Celebrity celebrity = celebrityRepository.findById(request.getCelebrityId())
                .orElseThrow(() -> new EntityNotFoundException("유명인이 없습니다. ID=" + request.getCelebrityId()));

        // 1. Quote 저장
        Quote quote = Quote.builder()
                .originalText(request.getOriginalText())
                .language(request.getLanguage())
                .contextScore(request.getContextScore())
                .celebrity(celebrity)
                .build();

        quoteRepository.save(quote);

        // 2. BookQuote 연결
        BookQuote bookQuote = BookQuote.builder()
                .book(book)
                .quote(quote)
                .build();
        bookQuoteRepository.save(bookQuote);

        // 3. QuoteSource 저장
        if (request.getSource() != null) {
            QuoteSource source = QuoteSource.builder()
                    .quote(quote)
                    .sourceUrl(request.getSource().getUrl())
                    .sourceType(request.getSource().getType())
                    .timestamp(request.getSource().getTimestamp())
                    .isDirectQuote(request.getSource().isDirect())
                    .build();
            quoteSourceRepository.save(source);
        }

        // 4. QuoteContext 저장
        if (request.getContext() != null) {
            QuoteContext context = QuoteContext.builder()
                    .quote(quote)
                    .contextHow(request.getContext().getHow())
                    .contextWhen(request.getContext().getWhen())
                    .contextWhy(request.getContext().getWhy())
                    .contextHelp(request.getContext().getHelp())
                    .build();
            quoteContextRepository.save(context);
        }
        applicationEventPublisher.publishEvent(
                new NotificationEvent.FollowEvent
                        (celebrity.getId(), celebrity.getName(),
                                book.getId(), book.getTitle(),book.getAuthorName()));

        return quote.getId();
    }

    // 조회 메서드들 (기존 유지, EntityNotFoundException 처리는 Repository 단계에서 안전하거나 Optional 처리됨)
    public List<QuoteResponse> getQuotesByBook(Long bookId) {
        return bookQuoteRepository.findByBookIdWithFetchJoin(bookId).stream() // (Book Repository 수정사항 반영: FetchJoin 메서드 사용 권장)
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public List<QuoteResponse> getQuotesByCelebrity(Long celebrityId) {
        return bookQuoteRepository.findByCelebrityIdWithFetchJoin(celebrityId).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    private QuoteResponse convertToResponse(BookQuote bq) {
        Quote quote = bq.getQuote();
        Book book = bq.getBook();
        Celebrity celebrity = quote.getCelebrity();

        QuoteContext context = quoteContextRepository.findByQuoteId(quote.getId()).orElse(null);
        QuoteSource source = quoteSourceRepository.findByQuoteId(quote.getId()).orElse(null);

        return QuoteResponse.of(quote, book, celebrity, context, source);
    }
}