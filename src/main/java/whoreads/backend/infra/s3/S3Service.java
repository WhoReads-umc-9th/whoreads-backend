package whoreads.backend.infra.s3;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Properties s3Properties;
    private final S3Presigner s3Presigner;

    /**
     * DB에 저장된 값(objectKey)을 클라이언트가 바로 쓸 수 있는 Presigned URL로 변환한다.
     * 버킷이 Private이어도 이 서명된 URL로만 정해진 시간 동안 접근 가능 — 봇이 celebrity/1, 2, 3...처럼
     * objectKey를 순회해도 서명 없인 403이라 스크래핑을 막는다.
     * 이미 http(s)로 시작하는 값(과도기 데이터)은 서명 없이 그대로 통과시킨다.
     */
    public String generateUrl(String objectKey) {
        if (objectKey == null || objectKey.isBlank()) {
            return null;
        }

        String key = objectKey.trim();

        if (key.startsWith("http://") || key.startsWith("https://")) {
            return key;
        }

        S3Properties.S3 s3 = s3Properties.getS3();

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(s3.getBucket())
                .key(key)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(s3.getPresignDuration())
                .getObjectRequest(getObjectRequest)
                .build();

        return s3Presigner.presignGetObject(presignRequest).url().toString();
    }
}
