package whoreads.backend.global.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import whoreads.backend.global.response.ApiResponse;

@RestController
@RequestMapping("/api")
public class HealthCheckController {

    @GetMapping("/health")
    public ApiResponse<String> healthCheck() {
        return ApiResponse.success("서버가 정상 동작 중입니다.", "OK");
    }
}
