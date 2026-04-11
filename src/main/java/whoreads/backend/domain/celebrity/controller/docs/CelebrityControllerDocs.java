package whoreads.backend.domain.celebrity.controller.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
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
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 파라미터 (ID가 1 이상이 아님)"), // 바꾼 이유: Validation 추가로 인한 400 에러 응답 문서화
            @ApiResponse(responseCode = "404", description = "존재하지 않는 유명인 ID", content = @Content) // 바꾼 이유: CustomException 추가로 인한 404 에러 응답 문서화
    })
    ResponseEntity<CelebrityResponse> getCelebrityById(
            @Parameter(description = "유명인 ID (1 이상)", required = true)
            @PathVariable @Positive(message = "올바른 유명인 ID를 입력해주세요.") Long id // 바꾼 이유: 파라미터 제약조건 문서화
    );
}