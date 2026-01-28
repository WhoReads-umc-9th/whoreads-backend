package whoreads.backend.domain.celebrity.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import whoreads.backend.domain.celebrity.controller.docs.CelebrityControllerDocs;
import whoreads.backend.domain.celebrity.dto.CelebrityResponse;
import whoreads.backend.domain.celebrity.entity.CelebrityTag;
import whoreads.backend.domain.celebrity.service.CelebrityService;

import java.util.List;

@RestController
@RequestMapping("/api/celebrities")
@RequiredArgsConstructor
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
    public ResponseEntity<CelebrityResponse> getCelebrityById(@PathVariable Long id) {
        return ResponseEntity.ok(celebrityService.getCelebrity(id));
    }
}