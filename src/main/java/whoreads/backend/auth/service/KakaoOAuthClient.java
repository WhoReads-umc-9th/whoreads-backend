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
import whoreads.backend.auth.dto.KakaoTokenResponseDto;
import whoreads.backend.auth.dto.KakaoUserInfoDto;
import whoreads.backend.global.exception.CustomException;
import whoreads.backend.global.exception.ErrorCode;

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
            log.error("카카오 사용자 정보 조회 중 통신 오류", e);
            throw new CustomException(ErrorCode.KAKAO_API_ERROR);
        }
    }

    /**
     * 브라우저 인가 코드(code)를 카카오 access_token으로 교환한다.
     * 로컬/스테이징에서 앱 없이 카카오 로그인을 테스트할 때만 사용.
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
            log.error("카카오 토큰 교환 중 통신 오류", e);
            throw new CustomException(ErrorCode.KAKAO_API_ERROR);
        }
    }
}
