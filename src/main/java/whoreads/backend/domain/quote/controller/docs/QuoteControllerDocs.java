package whoreads.backend.domain.quote.controller.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import whoreads.backend.domain.quote.dto.QuoteRequest;
import whoreads.backend.domain.quote.dto.QuoteResponse;

import java.util.List;

@Tag(name = "Quote (인용)", description = "유명인-책 인용 및 추천 정보 관리 API")
public interface QuoteControllerDocs {

    @Operation(summary = "인용 등록", description = "유명인이 책에 대해 언급한 인용(Quote)과 출처(Source), 맥락(Context) 정보를 한 번에 등록합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "인용 등록 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (책이나 유명인 ID 없음)"),
            @ApiResponse(responseCode = "500", description = "서버 에러")
    })
    ResponseEntity<Void> registerQuote(@RequestBody QuoteRequest request);

    @Operation(summary = "책별 인용 조회", description = "특정 책에 달린 유명인들의 인용 목록을 조회합니다. (맥락 점수 높은 순)")
    ResponseEntity<List<QuoteResponse>> getQuotesByBook(@Parameter(description = "책 ID") @PathVariable Long bookId);

    @Operation(summary = "인물별 인용 조회 (가상 서재)", description = "특정 유명인이 남긴 인용(읽은 책) 목록을 조회합니다. (맥락 점수 높은 순)")
    ResponseEntity<List<QuoteResponse>> getQuotesByCelebrity(@Parameter(description = "인물 ID") @PathVariable Long celebrityId);
}