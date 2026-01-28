package whoreads.backend.domain.celebrity.controller.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import whoreads.backend.domain.celebrity.dto.CelebrityResponse;
import whoreads.backend.domain.celebrity.entity.CelebrityTag;

import java.util.List;

@Tag(name = "Celebrity (유명인)", description = "유명인 조회 및 필터링 API")
public interface CelebrityControllerDocs {

    @Operation(summary = "유명인 목록 조회 (필터)", description = "유명인 전체 목록을 조회하거나, 직업 태그(tag)로 필터링하여 조회합니다.")
    ResponseEntity<List<CelebrityResponse>> getCelebrities(
            @Parameter(description = "직업 태그 (예: SINGER, ACTOR). 비워두면 전체 조회")
            @RequestParam(required = false) CelebrityTag tag
    );

    @Operation(summary = "유명인 상세 조회", description = "ID로 특정 유명인 정보를 조회합니다.")
    ResponseEntity<CelebrityResponse> getCelebrityById(
            @Parameter(description = "유명인 ID")
            @PathVariable Long id
    );
}