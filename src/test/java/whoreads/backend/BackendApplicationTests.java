package whoreads.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import whoreads.backend.config.TestFirebaseConfig;

@SpringBootTest
@Import(TestFirebaseConfig.class)
class BackendApplicationTests {

    @Test
    void contextLoads() {
    }

}
