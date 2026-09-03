package whoreads.backend.infra.s3;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@EnableConfigurationProperties(S3Properties.class)
public class S3Service {

    private final S3Properties s3Properties;

    /**
     * DB에 저장된 값(objectKey)을 클라이언트가 바로 쓸 수 있는 절대 URL로 변환한다.
     * 이미 http(s)로 시작하는 값은 그대로 통과시키므로 objectKey 전환 중 혼재해도 안전하다.
     */
    public String generateUrl(String objectKey) {
        if (objectKey == null || objectKey.isBlank()) {
            return null;
        }

        String key = objectKey.trim();

        if (key.startsWith("http://") || key.startsWith("https://")) {
            return key;
        }

        return resolveBaseUrl() + "/" + encodePath(key);
    }

    private String resolveBaseUrl() {
        S3Properties.S3 s3 = s3Properties.getS3();

        String baseUrl = (s3 != null) ? s3.getBaseUrl() : null;
        if (baseUrl != null && !baseUrl.isBlank()) {
            return trimTrailingSlash(baseUrl.trim());
        }

        return String.format("https://%s.s3.%s.amazonaws.com",
                (s3 != null) ? s3.getBucket() : null,
                s3Properties.getRegion());
    }

    private String trimTrailingSlash(String url) {
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    /**
     * 키에 공백이나 한글이 섞여도 깨지지 않도록 세그먼트 단위로 인코딩한다.
     * URLEncoder는 공백을 '+'로 바꾸므로 경로용인 %20으로 되돌린다.
     */
    private String encodePath(String key) {
        return Arrays.stream(key.split("/"))
                .map(segment -> URLEncoder.encode(segment, StandardCharsets.UTF_8).replace("+", "%20"))
                .collect(Collectors.joining("/"));
    }
}
