package whoreads.backend.global.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import whoreads.backend.global.exception.CustomException;
import whoreads.backend.global.exception.ErrorCode;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Slf4j
@Configuration
@Profile("!test")
public class FirebaseConfig {
    @Value("${firebase.secret.path}")
    private String serviceAccountPath;

    @Bean
    public FirebaseApp firebaseApp() {
        try {
            InputStream credentialsStream = getCredentialsStream();
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(credentialsStream))
                    .build();

            return FirebaseApp.initializeApp(options);

        } catch (IOException exception) {
            throw new CustomException(ErrorCode.FCM_SERVER_UNAVAILABLE);
        }
    }

    private InputStream getCredentialsStream() throws IOException {
        // 환경변수 우선 (배포 환경)
        String firebaseJson = System.getenv("FIREBASE_SECRET_JSON");
        if (firebaseJson != null && !firebaseJson.isEmpty()) {
            log.info("Loading Firebase credentials from environment variable");
            return new ByteArrayInputStream(firebaseJson.getBytes(StandardCharsets.UTF_8));
        }

        // 환경변수 없으면 파일에서 읽기 (로컬 환경)
        log.info("Loading Firebase credentials from classpath: {}", serviceAccountPath);
        return new ClassPathResource(serviceAccountPath).getInputStream();
    }

    @Bean
    public FirebaseMessaging firebaseMessaging(FirebaseApp firebaseApp) {
        return FirebaseMessaging.getInstance(firebaseApp);
    }
}