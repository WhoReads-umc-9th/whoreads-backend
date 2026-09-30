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
         * Presigned URL 유효시간의 "상한"일 뿐, 실제 만료는 이보다 먼저 올 수 있다.
         * EC2 인스턴스 프로필 자격증명은 그 자체가 임시(STS) 토큰이라, 서명에 쓰인 자격증명이
         * 먼저 만료되면 이 값과 무관하게 URL도 같이 무효화된다 — 그래서 인스턴스 프로필의 실제
         * 자격증명 수명(보통 6시간 안팎)보다 크게 길게 잡아봐야 의미가 없다. 기본 6시간.
         */
        private Duration presignDuration = Duration.ofHours(6);
    }
}
