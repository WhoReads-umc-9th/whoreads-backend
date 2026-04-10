package whoreads.backend.domain.quote.service;

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
import whoreads.backend.global.exception.CustomException;
import whoreads.backend.global.exception.ErrorCode;

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
    public Long registerQuote(QuoteRequest request) {
        // 바꾼 이유: EntityNotFoundException 대신 프로젝트 공통 예외인 CustomException과 정의된 ErrorCode 사용
        Book book = bookRepository.findById(request.getBookId())
                .orElseThrow(() -> new CustomException(ErrorCode.BOOK_NOT_FOUND));
        Celebrity celebrity = celebrityRepository.findById(request.getCelebrityId())
                .orElseThrow(() -> new CustomException(ErrorCode.CELEBRITY_NOT_FOUND));

        Quote quote = Quote.builder()
                .originalText(request.getOriginalText())
                .language(request.getLanguage())
                .contextScore(request.getContextScore())
                .celebrity(celebrity)
                .build();

        quoteRepository.save(quote);

        BookQuote bookQuote = BookQuote.builder()
                .book(book)
                .quote(quote)
                .build();
        bookQuoteRepository.save(bookQuote);

        if (request.getSource() != null) {
            QuoteSource source = QuoteSource.builder()
                    .quote(quote)
                    .sourceUrl(request.getSource().getUrl())
                    .sourceType(request.getSource().getType())
                    .timestamp(request.getSource().getTimestamp())
                    .build();
            quoteSourceRepository.save(source);
        }

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
                new NotificationEvent.FollowEvent(
                        celebrity.getId(), celebrity.getName(),
                        book.getId(), book.getTitle(), book.getAuthorName()
                )
        );

        return quote.getId();
    }

    // 조회 메서드들 (기존 유지, EntityNotFoundException 처리는 Repository 단계에서 안전하거나 Optional 처리됨)
    public List<QuoteResponse> getQuotesByBook(Long bookId) {
        return bookQuoteRepository.findByBookIdWithEntityGraph(bookId).stream()
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