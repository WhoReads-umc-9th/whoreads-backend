package whoreads.backend.auth.service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import whoreads.backend.auth.dto.KakaoTokenResponseDto;
import whoreads.backend.auth.dto.KakaoUserInfoDto;
import whoreads.backend.global.exception.CustomException;
import whoreads.backend.global.exception.ErrorCode;

/**
 * 카카오 인증 서버(kauth)/API 서버(kapi)와 통신하는 클라이언트
 *
 * [흐름]
 * 1. 클라이언트(앱)가 카카오 인가 코드(code)를 받아 백엔드로 전달
 * 2. requestToken(code)로 카카오 access_token 교환
 * 3. requestUserInfo(accessToken)로 사용자 정보(id, email, nickname) 조회
 */
@Slf4j
@Component
public class KakaoOAuthClient {

    private static final String TOKEN_URL = "https://kauth.kakao.com/oauth/token";
    private static final String USER_INFO_URL = "https://kapi.kakao.com/v2/user/me";

    private final RestTemplate restTemplate = createRestTemplate();

    @Value("${kakao.client-id}")
    private String clientId;

    // 카카오 디벨로퍼스 > 보안 탭에서 Client Secret을 발급/활성화하지 않았다면 비워둔다.
    @Value("${kakao.client-secret:}")
    private String clientSecret;

    @Value("${kakao.redirect-uri}")
    private String redirectUri;

    private RestTemplate createRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(3000);
        factory.setReadTimeout(3000);
        return new RestTemplate(factory);
    }

    // 서버가 실제로 어떤 카카오 설정값을 읽고 있는지 기동 시점에 확인하기 위한 로그 (시크릿 원문은 노출하지 않음)
    @PostConstruct
    private void logConfigOnStartup() {
        log.info("[카카오 설정 확인] client-id={}, client-secret 설정됨={}, redirect-uri={}",
                mask(clientId), clientSecret != null && !clientSecret.isBlank(), redirectUri);
    }

    private String mask(String value) {
        if (value == null || value.isBlank()) return "(비어있음)";
        if (value.length() <= 8) return "*** (len=" + value.length() + ")";
        return value.substring(0, 4) + "..." + value.substring(value.length() - 4) + " (len=" + value.length() + ")";
    }

    public KakaoTokenResponseDto requestToken(String code) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", clientId);
        body.add("redirect_uri", redirectUri);
        body.add("code", code);
        if (clientSecret != null && !clientSecret.isBlank()) {
            body.add("client_secret", clientSecret);
        }

        HttpEntity<MultiValueMap<String, String>> httpRequest = new HttpEntity<>(body, headers);

        try {
            KakaoTokenResponseDto response = restTemplate.postForObject(TOKEN_URL, httpRequest, KakaoTokenResponseDto.class);
            if (response == null || response.accessToken() == null) {
                // HTTP 200을 받았지만 응답 파싱 결과 access_token이 없는 경우 (DTO 매핑 문제 등)
                log.warn("카카오 토큰 응답에 access_token이 없습니다: {}", response);
                throw new CustomException(ErrorCode.KAKAO_API_ERROR);
            }
            return response;
        } catch (HttpClientErrorException e) {
            // 인가 코드 만료/재사용, redirect_uri 불일치, client_secret 누락 등 카카오 측 4xx 응답
            log.warn("카카오 토큰 발급 실패: status={}, body={}, client-id={}, client-secret 설정됨={}, redirect-uri={}",
                    e.getStatusCode(), e.getResponseBodyAsString(), mask(clientId),
                    clientSecret != null && !clientSecret.isBlank(), redirectUri);
            throw new CustomException(ErrorCode.KAKAO_API_ERROR);
        } catch (RestClientException e) {
            log.error("카카오 토큰 발급 중 통신 오류", e);
            throw new CustomException(ErrorCode.KAKAO_API_ERROR);
        }
    }

    public KakaoUserInfoDto requestUserInfo(String kakaoAccessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(kakaoAccessToken);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<Void> httpRequest = new HttpEntity<>(headers);

        try {
            ResponseEntity<KakaoUserInfoDto> response = restTemplate.exchange(
                    USER_INFO_URL, HttpMethod.GET, httpRequest, KakaoUserInfoDto.class);

            KakaoUserInfoDto userInfo = response.getBody();
            if (userInfo == null) {
                throw new CustomException(ErrorCode.KAKAO_API_ERROR);
            }
            return userInfo;
        } catch (RestClientException e) {
            log.error("카카오 사용자 정보 조회 중 통신 오류", e);
            throw new CustomException(ErrorCode.KAKAO_API_ERROR);
        }
    }
}
