package whoreads.backend.domain.library.controller.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import whoreads.backend.domain.library.dto.UserBookRequest;
import whoreads.backend.domain.library.dto.UserBookResponse;
import whoreads.backend.domain.library.enums.ReadingStatus;
import whoreads.backend.global.response.ApiResponse;

@Tag(name = "Library (내 서재)", description = "사용자의 서재 관리 API | by 쏘이/김서연")
public interface UserBookControllerDocs {

    @Operation(
            summary = "독서 기록 요약 조회",
            description = "완독한 책 수, 읽는 중인 책 수, 누적 독서 시간을 조회합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {
                                      "is_success": true,
                                      "code": 200,
                                      "message": "요청이 성공했습니다.",
                                      "result": {
                                        "completed_count": 5,
                                        "reading_count": 2,
                                        "total_read_minutes": 12345
                                      }
                                    }
                                    """)
                    )
            )
    })
    ResponseEntity<ApiResponse<UserBookResponse.Summary>> getLibrarySummary();

    @Operation(
            summary = "서재 책 목록 조회",
            description = "읽기 상태(wish/reading/complete)별로 책 목록을 커서 페이징으로 조회합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {
                                      "is_success": true,
                                      "code": 200,
                                      "message": "요청이 성공했습니다.",
                                      "result": {
                                        "books": [
                                          {
                                            "book": {
                                              "id": 1,
                                              "title": "클린 코드",
                                              "author_name": "로버트 C. 마틴",
                                              "cover_url": "https://image.aladin.co.kr/product/cover1.jpg",
                                              "total_page": 464
                                            },
                                            "reading_page": 150,
                                            "celebrities_count": 2,
                                            "celebrities": [
                                              { "id": 1, "profile_url": "https://example.com/profile1.jpg" },
                                              { "id": 2, "profile_url": "https://example.com/profile2.jpg" }
                                            ]
                                          },
                                          {
                                            "book": {
                                              "id": 2,
                                              "title": "이펙티브 자바",
                                              "author_name": "조슈아 블로크",
                                              "cover_url": "https://image.aladin.co.kr/product/cover2.jpg",
                                              "total_page": 412
                                            },
                                            "reading_page": null,
                                            "celebrities_count": 0,
                                            "celebrities": []
                                          }
                                        ],
                                        "next_cursor": 3,
                                        "has_next": true
                                      }
                                    }
                                    """)
                    )
            )
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
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "추가 성공",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {
                                      "is_success": true,
                                      "code": 201,
                                      "message": "책을 추가했습니다.",
                                      "result": {
                                        "user_book_id": 42
                                      }
                                    }
                                    """)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "책을 찾을 수 없음",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {
                                      "is_success": false,
                                      "code": 404,
                                      "message": "책을 찾을 수 없습니다."
                                    }
                                    """)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "이미 서재에 존재하는 책",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {
                                      "is_success": false,
                                      "code": 409,
                                      "message": "이미 서재에 존재하는 책입니다."
                                    }
                                    """)
                    )
            )
    })
    ResponseEntity<ApiResponse<UserBookResponse.AddResult>> addBookToLibrary(
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
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "변경 성공",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {
                                      "is_success": true,
                                      "code": 200,
                                      "message": "책 상태를 변경했습니다."
                                    }
                                    """)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (READING이 아닌데 reading_page 전송)",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {
                                      "is_success": false,
                                      "code": 400,
                                      "message": "READING 상태인 책만 reading_page를 변경할 수 있습니다."
                                    }
                                    """)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "책 추가 기록을 찾을 수 없음",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {
                                      "is_success": false,
                                      "code": 404,
                                      "message": "책 추가 기록을 찾을 수 없습니다."
                                    }
                                    """)
                    )
            )
    })
    ResponseEntity<ApiResponse<Void>> updateUserBook(
            @Parameter(description = "UserBook ID", required = true)
            Long userBookId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "변경할 상태 및 페이지 정보",
                    required = true
            )
            UserBookRequest.UpdateStatus request
    );

    @Operation(
            summary = "서재에서 책 삭제",
            description = "서재에서 책을 삭제합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "삭제 성공",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {
                                      "is_success": true,
                                      "code": 200,
                                      "message": "서재에서 책이 삭제되었습니다."
                                    }
                                    """)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "책 추가 기록을 찾을 수 없음",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {
                                      "is_success": false,
                                      "code": 404,
                                      "message": "책 추가 기록을 찾을 수 없습니다."
                                    }
                                    """)
                    )
            )
    })
    ResponseEntity<ApiResponse<Void>> deleteBookFromLibrary(
            @Parameter(description = "UserBook ID", required = true)
            Long userBookId
    );
}
