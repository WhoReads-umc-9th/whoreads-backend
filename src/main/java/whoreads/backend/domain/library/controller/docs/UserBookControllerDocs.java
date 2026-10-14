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
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증 필요",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {
                                      "is_success": false,
                                      "code": 401,
                                      "message": "인증이 필요합니다."
                                    }
                                    """)
                    )
            )
    })
    ResponseEntity<ApiResponse<UserBookResponse.Summary>> getLibrarySummary(
            @Parameter(hidden = true) Long memberId
    );

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
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증 필요",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {
                                      "is_success": false,
                                      "code": 401,
                                      "message": "인증이 필요합니다."
                                    }
                                    """)
                    )
            )
    })
    ResponseEntity<ApiResponse<UserBookResponse.BookList>> getBookList(
            @Parameter(hidden = true) Long memberId,
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
                    responseCode = "401",
                    description = "인증 필요",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {
                                      "is_success": false,
                                      "code": 401,
                                      "message": "인증이 필요합니다."
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
            @Parameter(hidden = true) Long memberId,
            @Parameter(description = "책 ID", required = true)
            Long bookId
    );

    @Operation(
            summary = "서재 책 읽기 정보 변경",
            description = """
                    서재에 있는 책의 읽기 상태, 시작일, 완료일, 읽은 페이지를 변경합니다.
                    모든 필드는 선택값이며, 보내지 않은(null) 필드는 기존 값을 유지합니다.
                    - 상태 버튼 클릭: { "reading_status": "READING" } 처럼 상태만 전송
                    - 상세 정보 수정: { "started_at": "2026-09-01", "reading_page": 120 } 처럼 필요한 필드만 전송
                    - reading_status: WISH, READING, COMPLETE (생략 시 현재 상태 유지)
                    - started_at, completed_at: READING/COMPLETE 상태에서만 입력 가능
                    - reading_page: READING 상태에서만 입력 가능
                    - 날짜 형식: yyyy-MM-dd, 미래 날짜 불가, 시작일 <= 완료일

                    서버는 날짜/페이지를 자동으로 설정하지 않습니다.
                    - 상태를 바꿔도 기존 시작일, 완료일, 읽은 페이지는 그대로 유지됩니다.
                    - 예외: READING에서 시작일만 보냈는데 저장된 완료일보다 늦으면, 시작일을 반영하고 완료일은 null로 초기화합니다.
                      (COMPLETE 상태이거나 완료일을 함께 보낸 경우에는 400)
                    - COMPLETE로 바꿔도 읽은 페이지를 전체 페이지로 바꾸지 않습니다.
                    - 날짜 기본값(예: 오늘)이나 다 읽음 시 페이지 처리는 프론트에서 결정해 함께 전송합니다.
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
                    description = "잘못된 요청 (상태에 맞지 않는 필드 입력, 미래 날짜, 시작일 > 완료일, 페이지 범위 초과)",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {
                                      "is_success": false,
                                      "code": 400,
                                      "message": "읽은 페이지는 1 이상이어야 합니다."
                                    }
                                    """)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증 필요",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {
                                      "is_success": false,
                                      "code": 401,
                                      "message": "인증이 필요합니다."
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
                                      "message": "서재에서 책을 찾을 수 없습니다."
                                    }
                                    """)
                    )
            )
    })
    ResponseEntity<ApiResponse<Void>> updateUserBook(
            @Parameter(hidden = true) Long memberId,
            @Parameter(description = "UserBook ID", required = true)
            Long userBookId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "변경할 읽기 정보 (보낸 필드만 수정)",
                    required = true
            )
            UserBookRequest.UpdateReadingInfo request
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
                    responseCode = "401",
                    description = "인증 필요",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {
                                      "is_success": false,
                                      "code": 401,
                                      "message": "인증이 필요합니다."
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
                                      "message": "서재에서 책을 찾을 수 없습니다."
                                    }
                                    """)
                    )
            )
    })
    ResponseEntity<ApiResponse<Void>> deleteBookFromLibrary(
            @Parameter(hidden = true) Long memberId,
            @Parameter(description = "UserBook ID", required = true)
            Long userBookId
    );
}
