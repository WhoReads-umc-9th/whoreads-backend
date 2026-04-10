package whoreads.backend.domain.book.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
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

    // 바꾼 이유: RestTemplateBuilder 임포트 에러 해결 및 @RequiredArgsConstructor 충돌 방지
    private final RestTemplate restTemplate = createRestTemplate();

    @Value("${aladin.api.key}")
    private String ttbKey;

    @Value("${aladin.api.url}")
    private String aladinUrl;

    // 타임아웃을 설정한 RestTemplate을 반환하는 메서드
    private RestTemplate createRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(3000); // 3000ms = 3초 (연결 타임아웃)
        factory.setReadTimeout(3000);    // 3000ms = 3초 (응답 타임아웃)
        return new RestTemplate(factory);
    }

    public List<BookResponse> searchBooks(String keyword) {  // 바꾼 이유: 키워드가 없는데 API를 찌르면 알라딘 쪽 에러가 발생하므로 서버 단에서 조기 차단
        if (keyword == null || keyword.isBlank()) {
            return Collections.emptyList();
        }

        try {
            String requestUrl = UriComponentsBuilder.fromUriString(aladinUrl)
                    .queryParam("ttbkey", ttbKey)
                    .queryParam("Query", keyword.trim())
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
                            .id(null) // DB에 저장 안 된 상태 (바꾼 이유: 0L보다 null이 'DB에 없음'을 논리적으로 명확히 나타냄)
                            .title(item.getTitle())
                            .authorName(item.getAuthor() != null ? item.getAuthor() : "저자 미상")
                            .coverUrl(item.getCover())
                            .totalPage(null)
                            .build())
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("알라딘 API 호출 중 에러 발생: ", e);
            return Collections.emptyList();
        }
    }
}