package whoreads.backend.infra.s3;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@Getter
@Setter
@ConfigurationProperties(prefix = "cloud.aws")
public class S3Properties {

    private String region;
    private S3 s3;

    @Getter
    @Setter
    public static class S3 {
        private String bucket;

        /**
         * Presigned URL 유효시간. 너무 짧으면 앱이 캐싱해둔 URL이 금방 만료되고,
         * 너무 길면 봇 접근 방지 효과가 떨어진다. 기본 24시간.
         */
        private Duration presignDuration = Duration.ofHours(24);
    }
}
