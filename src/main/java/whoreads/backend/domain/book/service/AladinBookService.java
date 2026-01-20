package whoreads.backend.domain.book.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import whoreads.backend.domain.book.dto.AladinBookResponse;
import whoreads.backend.domain.book.dto.BookResponse;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AladinBookService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${aladin.api.key}")
    private String ttbKey;

    @Value("${aladin.api.url}")
    private String aladinUrl;

    public List<BookResponse> searchBooks(String keyword) {
        try {
            String requestUrl = UriComponentsBuilder.fromUriString(aladinUrl)
                    .queryParam("ttbkey", ttbKey)
                    .queryParam("Query", keyword)
                    .queryParam("QueryType", "Title")
                    .queryParam("MaxResults", 10)
                    .queryParam("start", 1)
                    .queryParam("SearchTarget", "Book")
                    .queryParam("Output", "js")
                    .queryParam("Version", "20131101")
                    .build()
                    .toUriString();

            log.info("알라딘 요청 URL: {}", requestUrl);

            AladinBookResponse response = restTemplate.getForObject(requestUrl, AladinBookResponse.class);

            if (response == null || response.getItems() == null) {
                return Collections.emptyList();
            }

            return response.getItems().stream()
                    .map(item -> BookResponse.builder()
                            .id(0L)
                            .title(item.getTitle())
                            .authorName(item.getAuthor())
                            .coverUrl(item.getCover())
                            .build())
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("알라딘 API 호출 중 에러 발생: ", e);
            return Collections.emptyList();
        }
    }
}