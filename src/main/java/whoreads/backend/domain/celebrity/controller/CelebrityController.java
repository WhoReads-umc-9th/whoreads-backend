package whoreads.backend.domain.celebrity.controller;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import whoreads.backend.domain.celebrity.controller.docs.CelebrityControllerDocs;
import whoreads.backend.domain.celebrity.dto.CelebrityResponse;
import whoreads.backend.domain.celebrity.entity.CelebrityTag;
import whoreads.backend.domain.celebrity.service.CelebrityService;

import java.util.List;

@RestController
@RequestMapping("/api/celebrities")
@RequiredArgsConstructor
@Validated // 바꾼 이유: @PathVariable로 들어오는 id 값을 검증하기 위해 클래스 레벨에 활성화
public class CelebrityController implements CelebrityControllerDocs {

    private final CelebrityService celebrityService;

    // 1. 목록 조회 (전체 or 태그 필터)
    @Override
    @GetMapping
    public ResponseEntity<List<CelebrityResponse>> getCelebrities(
            @RequestParam(required = false) CelebrityTag tag) {
        return ResponseEntity.ok(celebrityService.getCelebrities(tag));
    }

    // 2. 상세 조회
    @Override
    @GetMapping("/{id}")
    public ResponseEntity<CelebrityResponse> getCelebrityById(
            @PathVariable @Positive(message = "올바른 유명인 ID를 입력해주세요.") Long id) {
        // 바꾼 이유: 음수나 0 같은 비정상적인 ID가 들어오는 것을 컨트롤러 단에서 미리 차단
        return ResponseEntity.ok(celebrityService.getCelebrity(id));
    }
}