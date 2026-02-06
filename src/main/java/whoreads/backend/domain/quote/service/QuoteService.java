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
    public void registerQuote(QuoteRequest request) {

        Book book = bookRepository.findById(request.getBookId())
                .orElseThrow(() -> new IllegalArgumentException("책이 없습니다. ID=" + request.getBookId()));
        Celebrity celebrity = celebrityRepository.findById(request.getCelebrityId())
                .orElseThrow(() -> new IllegalArgumentException("유명인이 없습니다. ID=" + request.getCelebrityId()));

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
                    .contextWhen(request.getContext().getWhen()) // [추가]
                    .contextWhy(request.getContext().getWhy())
                    .contextHelp(request.getContext().getHelp())
                    .build();
            quoteContextRepository.save(context);
        }
        applicationEventPublisher.publishEvent(
                new NotificationEvent.FollowEvent
                        (celebrity.getId(), celebrity.getName(),
                                book.getId(), book.getTitle(),book.getAuthorName()));
    }

    // 1. 책 ID로 인용 목록 조회 (책 상세 페이지용)
    public List<QuoteResponse> getQuotesByBook(Long bookId) {
        return bookQuoteRepository.findByBookIdOrderByQuoteContextScoreDesc(bookId).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // 2. 인물 ID로 인용 목록 조회 (가상 서재용)
    public List<QuoteResponse> getQuotesByCelebrity(Long celebrityId) {
        return bookQuoteRepository.findByQuote_Celebrity_IdOrderByQuoteContextScoreDesc(celebrityId).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // [내부 메서드] BookQuote 엔티티를 DTO로 변환하는 로직
    private QuoteResponse convertToResponse(BookQuote bq) {
        Quote quote = bq.getQuote();
        Book book = bq.getBook();
        Celebrity celebrity = quote.getCelebrity();

        // 맥락(Context)과 출처(Source) 가져오기
        // (단방향 매핑이라 레포지토리에서 별도로 조회)
        QuoteContext context = quoteContextRepository.findByQuoteId(quote.getId()).orElse(null);
        QuoteSource source = quoteSourceRepository.findByQuoteId(quote.getId()).orElse(null);

        return QuoteResponse.of(quote, book, celebrity, context, source);
    }
}