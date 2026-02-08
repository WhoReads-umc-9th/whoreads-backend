package whoreads.backend.domain.readingsession.controller.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import whoreads.backend.domain.readingsession.dto.ReadingSessionResponse;
import whoreads.backend.global.response.ApiResponse;

@Tag(name = "Reading Session - Records", description = "독서 기록/통계 API | by 쏘이/김서연")
public interface ReadingSessionStatsControllerDocs {

    @Operation(
            summary = "오늘의 독서 시간 조회",
            description = """
                    오늘의 독서 시간과 어제와의 차이를 조회합니다.
                    - 날짜 경계(00:00)를 기준으로 시간을 분할하여 계산합니다.
                    - 예: 23:50~00:10 인터벌은 어제 10분, 오늘 10분으로 분리됩니다.
                    - difference_from_yesterday: 양수면 오늘이 어제보다 많음, 음수면 오늘이 어제보다 적음
                    """
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
                                        "today_minutes": 45,
                                        "difference_from_yesterday": 15
                                      }
                                    }
                                    """)
                    )
            )
    })
    ResponseEntity<ApiResponse<ReadingSessionResponse.TodayFocus>> getTodayFocus(
            Long memberId
    );

    @Operation(
            summary = "총 집중 시간 조회",
            description = "사용자의 전체 독서 시간(분)을 조회합니다."
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
                                        "total_minutes": 1234
                                      }
                                    }
                                    """)
                    )
            )
    })
    ResponseEntity<ApiResponse<ReadingSessionResponse.TotalFocus>> getTotalFocus(
            Long memberId
    );
}
