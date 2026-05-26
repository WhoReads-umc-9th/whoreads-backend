package whoreads.backend.domain.book.controller.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import whoreads.backend.domain.book.dto.BookDetailResponse;
import whoreads.backend.domain.book.dto.BookRequest;
import whoreads.backend.domain.book.dto.BookResponse;
import whoreads.backend.domain.topic.entity.TopicTag;

import java.util.List;

@Tag(name = "Book (책)", description = "책 조회 및 검색 API")
public interface BookControllerDocs {

    @Operation(summary = "도서 목록 조회 및 검색 (내부 DB)", description = "우리 DB에 저장된 도서 목록을 조회합니다. keyword가 있으면 제목/저자로 검색합니다.")
    List<BookResponse> getAllBooks(
            @Parameter(description = "검색어 (비워두면 전체 조회)") @RequestParam(required = false) String keyword
    );

    @Operation(summary = "알라딘 도서 검색", description = "알라딘 API를 사용하여 외부 도서를 검색합니다. (검색어 필수)")
    List<BookResponse> aladinSearchBooks(
            @Parameter(description = "검색어 (공백 불가)", required = true)
            @RequestParam @NotBlank(message = "알라딘 검색어는 필수입니다.") String keyword
    );

    @Operation(summary = "책 등록 (중복 체크)", description = "책 정보를 등록합니다. 만약 제목과 작가가 동일한 책이 이미 있다면, 새로 저장하지 않고 기존 책 정보를 반환합니다.")
    ResponseEntity<BookResponse> registerBook(
            @RequestBody @Valid BookRequest request
    );

    @Operation(summary = "가장 많이 추천된 책 (TOP 20)", description = "유명인들이 가장 많이 언급(인용)한 책들을 추천 수 내림차순으로 조회합니다. limit은 1 이상이어야 합니다.")
    ResponseEntity<List<BookResponse>> getMostRecommendedBooks(
            @Parameter(description = "가져올 책 개수 (기본값 20, 1 이상)")
            @RequestParam(defaultValue = "20") @Positive(message = "가져올 개수는 1 이상이어야 합니다.") int limit
    );

    @Operation(
            summary = "책 상세페이지 조회",
            description = "책 상세 정보를 조회합니다. 책 기본 정보, 관련 인용(유명인, 출처 포함), 추천 수를 반환합니다.\n\n"
                    + "로그인한 사용자가 담아둔 책이면 reading_info(읽기 상태, 진행 페이지, 시작/종료 날짜)도 함께 반환합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 파라미터 (ID가 양수가 아님)"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 책 ID", content = @Content)
    })
    whoreads.backend.global.response.ApiResponse<BookDetailResponse> getBookDetail(
            @Parameter(description = "책 ID (1 이상)", required = true)
            @PathVariable @Positive(message = "올바른 책 ID를 입력해주세요.") Long bookId
    );

    @Operation(summary = "주제별 추천 책 목록 조회", description = "특정 주제(SOCIETY, HUMAN_UNDERSTANDING 등)에 맞는 유명인 추천 책 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 TopicTag 또는 limit 값", content = @Content),
            @ApiResponse(responseCode = "404", description = "해당 주제의 책 없음", content = @Content),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    })
    ResponseEntity<List<BookResponse>> getBooksByTheme(
            @Parameter(description = "주제 태그", required = true) @PathVariable TopicTag theme,
            @Parameter(description = "가져올 개수 (1 이상)") @RequestParam(defaultValue = "20") @Positive int limit
    );
}