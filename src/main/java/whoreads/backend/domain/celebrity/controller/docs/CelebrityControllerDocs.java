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
import whoreads.backend.domain.celebrity.dto.CelebrityCategoryResponse;
import whoreads.backend.domain.celebrity.dto.CelebrityDetailResponse;
import whoreads.backend.domain.celebrity.dto.CelebrityResponse;
import whoreads.backend.domain.celebrity.entity.CelebrityTag;

import java.util.List;

@Tag(name = "Celebrity (유명인)", description = "유명인 조회 및 필터링 API")
public interface CelebrityControllerDocs {

    @Operation(summary = "인물 카테고리 목록 조회", description = "유명인 직업 태그(카테고리) 전체 목록을 code/name 형태로 조회합니다. 프론트 하드코딩 대체용입니다.")
    ResponseEntity<List<CelebrityCategoryResponse>> getCelebrityCategories();

    @Operation(summary = "유명인 목록 조회 (필터)", description = "유명인 전체 목록을 조회하거나, 직업 태그(tag)로 필터링하여 조회합니다.")
    ResponseEntity<List<CelebrityResponse>> getCelebrities(
            @Parameter(description = "직업 태그 (예: SINGER, ACTOR). 비워두면 전체 조회")
            @RequestParam(required = false) CelebrityTag tag
    );

    @Operation(summary = "유명인 상세 조회", description = """
            ID로 특정 유명인 정보를 조회합니다.

            `image_attribution`은 세 가지 형태 중 하나로 내려갑니다(링크가 아닌 순수 텍스트이며, null은 없습니다):
            - "자유 이용 가능": 퍼블릭 도메인 또는 CC0 — 표시 의무는 없지만 저작권 상태를 알려줍니다.
            - "{저작자} · {라이선스} {버전} · 편집됨\\n{원본 URL}": 표시 의무가 있고 저작자 정보가 있는 경우.
              원본 URL은 줄바꿈(\\n)으로 구분됩니다.
            - "" (빈 문자열): 표시 의무는 있으나 저작자 정보가 없는 경우(출처 미확인 인물) — 확인이 필요한 상태입니다.
            """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 파라미터 (ID가 1 이상이 아님)"), // 바꾼 이유: Validation 추가로 인한 400 에러 응답 문서화
            @ApiResponse(responseCode = "404", description = "존재하지 않는 유명인 ID", content = @Content) // 바꾼 이유: CustomException 추가로 인한 404 에러 응답 문서화
    })
    ResponseEntity<CelebrityDetailResponse> getCelebrityById(
            @Parameter(description = "유명인 ID (1 이상)", required = true)
            @PathVariable @Positive(message = "올바른 유명인 ID를 입력해주세요.") Long id // 바꾼 이유: 파라미터 제약조건 문서화
    );
}
