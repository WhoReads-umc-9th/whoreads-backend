package whoreads.backend.infra.s3;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
@EnableConfigurationProperties(S3Properties.class)
public class S3Config {

    /**
     * Presigned URL 서명 전용. 자격증명은 기본 체인(DefaultCredentialsProvider)을 그대로 따른다.
     * 로컬 개발은 AWS_PROFILE=WhoReads, 배포 서버(EC2)는 인스턴스에 연결된 IAM Role로 자동 인증 —
     * 정적 액세스 키를 서버에 넣지 않는다. 이 코드는 두 경우 모두 변경 없이 동작한다.
     */
    @Bean
    public S3Presigner s3Presigner(S3Properties s3Properties) {
        return S3Presigner.builder()
                .region(Region.of(s3Properties.getRegion()))
                .build();
    }
}
