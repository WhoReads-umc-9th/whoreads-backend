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
    }
}
