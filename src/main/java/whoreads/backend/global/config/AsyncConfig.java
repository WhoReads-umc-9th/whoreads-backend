package whoreads.backend.global.config;

import java.lang.reflect.Method;
import java.util.concurrent.Executor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Slf4j
@EnableAsync
@Configuration
public class AsyncConfig implements AsyncConfigurer {

    // 스레드 정의
    @Bean(name = "WhoReadsAsyncExecutor")
    public ThreadPoolTaskExecutor asyncTaskExecutor() {
        // 현재 서버의 CPU 코어 수를 가져옵니다.
        int processors = Runtime.getRuntime().availableProcessors();
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 기본 유지 스레드
        executor.setCorePoolSize(processors);

        // 최대 스레드
        executor.setMaxPoolSize(processors * 2);

        // 대기 큐 (알림이 밀릴 경우 대비)
        executor.setQueueCapacity(100);

        // 로그용 식별자
        executor.setThreadNamePrefix("WhoReads-Async");

        // 빈으로 등록하기 위해 초기화합니다.
        executor.initialize();
        return executor;
    }

    // @async 어노테이션 설정시 자동으로 위의 executor 반환하게 설정
    @Override
    public Executor getAsyncExecutor() {
        return asyncTaskExecutor();
    }

    // 에러 처리 핸들러
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new AsyncExceptionHandler();
    }

    // 스레드 에러 핸들러
    @Slf4j
    private static class AsyncExceptionHandler implements AsyncUncaughtExceptionHandler {
        @Override
        public void handleUncaughtException(Throwable ex, Method method, Object... params) {
            log.error("====================================================");
            log.error("[비동기 에러] 메서드: {}", method.getName());

            if (params != null) {
                for (int i = 0; i < params.length; i++) {
                    log.error("파라미터 [{}]: {}", i, params[i]);
                }
            }

            log.error("에러 메시지: {}", ex.getMessage());
            log.error("상세 내용: ", ex);
            log.error("====================================================");
        }
    }
}