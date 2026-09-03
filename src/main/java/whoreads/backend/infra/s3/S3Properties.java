package whoreads.backend.infra.s3;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

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
         * 이미지 배포 기준 도메인. CloudFront 등을 앞에 두면 이 값만 바꾸면 되고 DB는 건드리지 않는다.
         * 비어 있으면 S3 기본 엔드포인트를 사용한다. (예: https://cdn.whoreads.app)
         */
        private String baseUrl;
    }
}
