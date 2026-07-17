package whoreads.backend.auth.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import whoreads.backend.auth.dto.KakaoUserInfoDto;
import whoreads.backend.global.exception.CustomException;
import whoreads.backend.global.exception.ErrorCode;

/**
 * 카카오 API 서버(kapi)와 통신하는 클라이언트
 *
 * 앱(iOS/Android)의 카카오 SDK가 이미 발급받은 access_token을 그대로 전달받아
 * kapi.kakao.com/v2/user/me로 사용자 정보(id, email, nickname)를 조회한다.
 */
@Slf4j
@Component
public class KakaoOAuthClient {

    private static final String USER_INFO_URL = "https://kapi.kakao.com/v2/user/me";

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
}
