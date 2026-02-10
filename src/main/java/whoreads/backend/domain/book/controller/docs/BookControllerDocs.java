package whoreads.backend.domain.book.controller.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import whoreads.backend.domain.book.dto.BookDetailResponse;
import whoreads.backend.domain.book.dto.BookRequest;
import whoreads.backend.domain.book.dto.BookResponse;

import java.util.List;

@Tag(name = "Book (책)", description = "책 조회 및 검색 API")
public interface BookControllerDocs {

    @Operation(summary = "도서 목록 조회 및 검색 (내부 DB)", description = "우리 DB에 저장된 도서 목록을 조회합니다. keyword가 있으면 제목/저자로 검색합니다.")
    List<BookResponse> getAllBooks(@Parameter(description = "검색어 (비워두면 전체 조회)") @RequestParam(required = false) String keyword);

    @Operation(summary = "책 상세 조회", description = "책 ID로 상세 정보를 조회합니다.")
    BookResponse getBook(@Parameter(description = "책 ID") @PathVariable Long bookId);

    @Operation(summary = "책 검색 (알라딘)", description = "제목 또는 저자로 책을 알라딘으로 검색합니다.")
    List<BookResponse> aladinSearchBooks(@Parameter(description = "검색어") @RequestParam String keyword);

    @Operation(summary = "책 등록 (중복 체크)", description = "책 정보를 등록합니다. 만약 제목과 작가가 동일한 책이 이미 있다면, 새로 저장하지 않고 기존 책 정보를 반환합니다.")
    ResponseEntity<BookResponse> registerBook(@RequestBody BookRequest request);

    @Operation(summary = "가장 많이 추천된 책 (TOP 20)", description = "유명인들이 가장 많이 언급(인용)한 책들을 추천 수 내림차순으로 조회합니다.")
    ResponseEntity<List<BookResponse>> getMostRecommendedBooks(@Parameter(description = "가져올 책 개수 (기본값 20)") @RequestParam(defaultValue = "20") int limit);

    @Operation(
            summary = "책 상세페이지 조회",
            description = "책 상세 정보를 조회합니다. 책 기본 정보, 관련 인용(유명인, 출처 포함), 추천 수를 반환합니다.\n\n"
                    + "로그인한 사용자가 담아둔 책이면 reading_info(읽기 상태, 진행 페이지, 시작/종료 날짜)도 함께 반환합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "is_success": true,
                                      "code": 200,
                                      "message": "요청이 성공했습니다.",
                                      "result": {
                                        "id": 1,
                                        "title": "사피엔스",
                                        "author_name": "유발 하라리",
                                        "genre": "인문",
                                        "cover_url": "https://image.aladin.co.kr/product/4860/35/cover/8934972467_2.jpg",
                                        "link": "https://www.aladin.co.kr/shop/wproduct.aspx?ItemId=48603543",
                                        "quote_count": 2,
                                        "reading_info": null,
                                        "quotes": [
                                          {
                                            "quote_id": 10,
                                            "original_text": "역사의 가장 큰 거짓말은 우리가 진보하고 있다는 믿음이다.",
                                            "context_score": 5,
                                            "celebrity": {
                                              "id": 5,
                                              "name": "빌 게이츠",
                                              "image_url": "https://example.com/billgates.jpg",
                                              "job_tags": ["기업가"]
                                            },
                                            "source": {
                                              "url": "https://youtube.com/watch?v=example",
                                              "type": "VIDEO",
                                              "timestamp": "03:25"
                                            }
                                          },
                                          {
                                            "quote_id": 15,
                                            "original_text": "이 책은 우리가 어디서 왔는지를 가장 명쾌하게 설명한다.",
                                            "context_score": 3,
                                            "celebrity": {
                                              "id": 8,
                                              "name": "오바마",
                                              "image_url": "https://example.com/obama.jpg",
                                              "job_tags": ["정치인", "대통령"]
                                            },
                                            "source": {
                                              "url": "https://twitter.com/BarackObama/status/example",
                                              "type": "SOCIAL_MEDIA",
                                              "timestamp": null
                                            }
                                          }
                                        ]
                                      }
                                    }
                                    """))),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 책 ID",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "is_success": false,
                                      "code": 404,
                                      "message": "책을 찾을 수 없습니다."
                                    }
                                    """)))
    })
    whoreads.backend.global.response.ApiResponse<BookDetailResponse> getBookDetail(
            @Parameter(description = "책 ID") @PathVariable Long bookId,
            @AuthenticationPrincipal Long memberId
    );
}