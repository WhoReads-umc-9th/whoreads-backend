package whoreads.backend.domain.celebrity.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import whoreads.backend.domain.celebrity.dto.CelebrityResponse;

import java.util.List;

@RestController
@RequestMapping("/api/celebrities")
@RequiredArgsConstructor
public class CelebrityController {

    @GetMapping
    public List<CelebrityResponse> getAllCelebrities() {
        return List.of(); // 나중에 Service 연결 > 지금은 빈 리스트
    }

    @GetMapping("/{id}")
    public CelebrityResponse getCelebrityById(@PathVariable Long id) {
        // 가짜 데이터 예시 (나중에 지울 것)
        return CelebrityResponse.builder()
                .id(id)
                .name("페이커")
                .shortBio("LoL 프로게이머")
                .tags("운동선수")
                .imageUrl("https://example.com/faker.jpg")
                .build();
    }
}