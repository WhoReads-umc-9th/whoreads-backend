package whoreads.backend.infra.s3;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.net.URI;

@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Properties s3Properties;
    private final S3Presigner s3Presigner;

    /**
     * DB에 저장된 값(objectKey)을 클라이언트가 바로 쓸 수 있는 Presigned URL로 변환한다.
     * 버킷이 Private이어도 이 서명된 URL로만 정해진 시간 동안 접근 가능 — 봇이 celebrity/1, 2, 3...처럼
     * objectKey를 순회해도 서명 없인 403이라 스크래핑을 막는다.
     *
     * 유효시간은 설정값(presignDuration)을 상한으로 요청하지만, 실제 만료는 그보다 먼저 올 수 있다 —
     * EC2 인스턴스 프로필로 받은 자격증명은 그 자체가 임시(STS) 토큰이라, 서명 시점에 쓰인 자격증명이
     * 먼저 만료되면 X-Amz-Expires와 무관하게 URL도 함께 무효화된다(AWS 공식 동작). 인스턴스 프로필
     * 자격증명은 보통 몇 시간 단위로 갱신되므로, presignDuration을 그보다 훨씬 길게 잡아도 실제로는
     * 더 일찍 끊길 수 있다는 뜻이다. 클라이언트가 만료된 URL을 만나면 그냥 API를 다시 호출해 새 URL을
     * 받으면 되므로(코드 변경 불필요) 별도 갱신 경로는 두지 않았다.
     */
    public String generateUrl(String objectKey) {
        if (objectKey == null || objectKey.isBlank()) {
            return null;
        }

        String key = objectKey.trim();

        if (key.startsWith("http://") || key.startsWith("https://")) {
            String ownBucketKey = extractKeyIfOwnBucket(key);
            if (ownBucketKey == null) {
                // 진짜 외부 URL(Wikimedia Commons, 유튜브 썸네일 등, 과도기 데이터)은 서명 없이 통과
                return key;
            }
            key = ownBucketKey;
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

    /**
     * image_url에 objectKey 대신 우리 버킷을 직접 가리키는 전체 URL이 들어있는 경우를 대비한다.
     * 버킷이 Private로 바뀐 뒤에는 그런 URL을 서명 없이 그대로 내보내면 항상 403이 나므로,
     * objectKey를 뽑아내 정상적으로 재서명한다. 다른 도메인의 URL은 null을 돌려줘 그대로 통과시킨다.
     */
    private String extractKeyIfOwnBucket(String url) {
        String bucket = s3Properties.getS3().getBucket();
        try {
            URI uri = URI.create(url);
            String host = uri.getHost();
            String path = uri.getRawPath();
            if (host == null || path == null || !path.startsWith("/")) {
                return null;
            }

            // 버추얼 호스트 스타일: {bucket}.s3.{region}.amazonaws.com/{key}
            if (host.startsWith(bucket + ".s3.")) {
                return path.substring(1);
            }
            // 패스 스타일: s3.{region}.amazonaws.com/{bucket}/{key}
            if (host.startsWith("s3.") || host.startsWith("s3-")) {
                String prefix = "/" + bucket + "/";
                if (path.startsWith(prefix)) {
                    return path.substring(prefix.length());
                }
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }
}
