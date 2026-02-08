package whoreads.backend.domain.topic.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import whoreads.backend.domain.book.entity.Book;
import whoreads.backend.domain.book.entity.BookQuote;
import whoreads.backend.domain.book.repository.BookQuoteRepository;
import whoreads.backend.domain.celebrity.entity.Celebrity;
import whoreads.backend.domain.quote.dto.QuoteResponse;
import whoreads.backend.domain.quote.entity.Quote;
import whoreads.backend.domain.quote.entity.QuoteContext;
import whoreads.backend.domain.quote.entity.QuoteSource;
import whoreads.backend.domain.quote.repository.QuoteContextRepository;
import whoreads.backend.domain.quote.repository.QuoteSourceRepository;
import whoreads.backend.domain.topic.dto.TopicResponse;
import whoreads.backend.domain.topic.entity.Topic;
import whoreads.backend.domain.topic.repository.TopicQuoteRepository;
import whoreads.backend.domain.topic.repository.TopicRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TopicService {

    private final TopicRepository topicRepository;
    private final TopicQuoteRepository topicQuoteRepository;

    // QuoteResponse 생성을 위한 리포지토리들
    private final BookQuoteRepository bookQuoteRepository; // Book 찾기용
    private final QuoteContextRepository quoteContextRepository;
    private final QuoteSourceRepository quoteSourceRepository;

    public List<TopicResponse> getAllTopics() {
        List<Topic> topics = topicRepository.findAll();
        List<TopicResponse> responses = new ArrayList<>();

        for (Topic topic : topics) {
            // 해당 주제의 인용들 가져오기
            List<QuoteResponse> quoteResponses = topicQuoteRepository.findByTopicWithFetchJoin(topic).stream()
                    .map(tq -> convertToQuoteResponse(tq.getQuote()))
                    .collect(Collectors.toList());

            responses.add(TopicResponse.of(topic, quoteResponses));
        }

        return responses;
    }

    private QuoteResponse convertToQuoteResponse(Quote quote) {
        // 1. Quote와 연결된 Book 찾기 (BookQuote 테이블 조회)
        // (Quote 하나는 하나의 책에만 연결된다고 가정하면 findFirst 사용)
        Book book = bookQuoteRepository.findByQuoteId(quote.getId())
                .map(BookQuote::getBook)
                .orElseThrow(() -> new IllegalStateException("인용에 연결된 책 정보가 없습니다. QuoteID=" + quote.getId()));

        Celebrity celebrity = quote.getCelebrity();
        QuoteContext context = quoteContextRepository.findByQuoteId(quote.getId()).orElse(null);
        QuoteSource source = quoteSourceRepository.findByQuoteId(quote.getId()).orElse(null);

        return QuoteResponse.of(quote, book, celebrity, context, source);
    }
}