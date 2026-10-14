package whoreads.backend.auth.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import whoreads.backend.auth.dto.KakaoTokenResponseDto;
import whoreads.backend.auth.dto.KakaoUserInfoDto;
import whoreads.backend.global.exception.CustomException;
import whoreads.backend.global.exception.ErrorCode;

import java.net.URI;

/**
 * 카카오 API 서버(kapi/kauth)와 통신하는 클라이언트
 *
 * 앱(iOS/Android)의 카카오 SDK가 이미 발급받은 access_token을 그대로 전달받아
 * kapi.kakao.com/v2/user/me로 사용자 정보(id, email, nickname)를 조회한다.
 *
 * 추가로, 브라우저 기반 인가 코드(code) 방식 로그인을 로컬에서 테스트할 수 있도록
 * kauth.kakao.com/oauth/token으로 code -> access_token 교환도 지원한다.
 */
@Slf4j
@Component
public class KakaoOAuthClient {

    private static final String USER_INFO_URL = "https://kapi.kakao.com/v2/user/me";
    private static final String TOKEN_URL = "https://kauth.kakao.com/oauth/token";

    @Value("${kakao.rest-api-key}")
    private String restApiKey;

    @Value("${kakao.client-secret:}")
    private String clientSecret;

    @Value("${kakao.redirect-uri}")
    private String redirectUri;

    private final RestTemplate restTemplate = createRestTemplate();

    /** 앱은 키 없이 서버의 이 인가 진입점을 사용한다. Client Secret은 포함하지 않는다. */
    public URI authorizationUri(String state) {
        if (state == null || !state.matches("[A-Za-z0-9_-]{43,128}")) {
            throw new CustomException(ErrorCode.INVALID_OAUTH_STATE);
        }
        URI callback;
        try {
            callback = URI.create(redirectUri);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new CustomException(ErrorCode.KAKAO_API_ERROR);
        }
        if (restApiKey == null || !restApiKey.matches("[a-fA-F0-9]{32}")
                || !"https".equals(callback.getScheme()) || callback.getHost() == null
                || callback.getUserInfo() != null || callback.getQuery() != null
                || callback.getFragment() != null
                || !"/api/auth/kakao/callback".equals(callback.getPath())) {
            throw new CustomException(ErrorCode.KAKAO_API_ERROR);
        }
        return UriComponentsBuilder.fromUriString("https://kauth.kakao.com/oauth/authorize")
                .queryParam("client_id", restApiKey)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("response_type", "code")
                .queryParam("state", state)
                .build().encode().toUri();
    }

    private RestTemplate createRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(3000);
        factory.setReadTimeout(3000);
        return new RestTemplate(factory);
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
            // 카카오 오류 응답 본문에 토큰 등이 들어갈 수 있으므로 예외 원문을 기록하지 않는다.
            log.error("카카오 사용자 정보 조회 중 통신 오류");
            throw new CustomException(ErrorCode.KAKAO_API_ERROR);
        }
    }

    /**
     * 브라우저 인가 코드(code)를 카카오 access_token으로 교환한다.
     * Client Secret은 서버 환경변수에서만 읽고 서버 간 토큰 요청에만 전달한다.
     */
    public String exchangeCodeForAccessToken(String code) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", restApiKey);
        body.add("redirect_uri", redirectUri);
        body.add("code", code);
        if (clientSecret != null && !clientSecret.isBlank()) {
            body.add("client_secret", clientSecret);
        }

        HttpEntity<MultiValueMap<String, String>> httpRequest = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<KakaoTokenResponseDto> response = restTemplate.exchange(
                    TOKEN_URL, HttpMethod.POST, httpRequest, KakaoTokenResponseDto.class);

            KakaoTokenResponseDto tokenResponse = response.getBody();
            if (tokenResponse == null || tokenResponse.accessToken() == null) {
                throw new CustomException(ErrorCode.KAKAO_API_ERROR);
            }
            return tokenResponse.accessToken();
        } catch (RestClientException e) {
            log.error("카카오 토큰 교환 중 통신 오류");
            throw new CustomException(ErrorCode.KAKAO_API_ERROR);
        }
    }
}
