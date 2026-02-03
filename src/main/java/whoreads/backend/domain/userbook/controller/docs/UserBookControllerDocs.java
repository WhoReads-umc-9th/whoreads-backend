package whoreads.backend.domain.userbook.controller.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import whoreads.backend.domain.userbook.dto.UserBookRequest;
import whoreads.backend.domain.userbook.dto.UserBookResponse;
import whoreads.backend.domain.userbook.enums.ReadingStatus;
import whoreads.backend.global.response.ApiResponse;

@Tag(name = "UserBook (내 서재)", description = "사용자의 서재 관리 API")
public interface UserBookControllerDocs {

    @Operation(
            summary = "독서 기록 요약 조회",
            description = "완독한 책 수, 읽는 중인 책 수, 누적 독서 시간을 조회합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공")
    })
    ResponseEntity<ApiResponse<UserBookResponse.Summary>> getLibrarySummary();

    @Operation(
            summary = "서재 책 목록 조회",
            description = "읽기 상태(wish/reading/complete)별로 책 목록을 커서 페이징으로 조회합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공")
    })
    ResponseEntity<ApiResponse<UserBookResponse.BookList>> getBookList(
            @Parameter(description = "읽기 상태 (WISH, READING, COMPLETE)", required = true)
            ReadingStatus status,
            @Parameter(description = "커서 (이전 응답의 nextCursor 값)")
            Long cursor,
            @Parameter(description = "페이지 크기 (기본값: 10)")
            Integer size
    );

    @Operation(
            summary = "서재에 책 추가",
            description = "책을 내 서재에 추가합니다. 기본 상태는 WISH(담아둠)입니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "추가 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "책을 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 서재에 존재하는 책")
    })
    ResponseEntity<ApiResponse<UserBookResponse.Detail>> addBookToLibrary(
            @Parameter(description = "책 ID", required = true)
            Long bookId
    );

    @Operation(
            summary = "서재 책 상태/페이지 변경",
            description = """
                    서재에 있는 책의 읽기 상태와 읽은 페이지를 변경합니다.
                    - reading_status: 필수 (WISH, READING, COMPLETE)
                    - reading_page: 선택 (status가 READING일 때만 변경 가능)
                    - status가 READING이 아닌데 reading_page를 보내면 400 에러
                    - status가 WISH/COMPLETE로 변경되어도 기존 reading_page는 유지됨
                    """
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "변경 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청 (READING이 아닌데 reading_page 전송)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "서재에서 책을 찾을 수 없음")
    })
    ResponseEntity<ApiResponse<UserBookResponse.Detail>> updateUserBook(
            @Parameter(description = "UserBook ID", required = true)
            Long userBookId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "변경할 상태 및 페이지 정보",
                    required = true
            )
            UserBookRequest.UpdateStatus request
    );
}
