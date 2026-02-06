package whoreads.backend.domain.readingsession.controller.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import whoreads.backend.domain.readingsession.dto.ReadingSessionResponse;
import whoreads.backend.global.response.ApiResponse;

@Tag(name = "Reading Session - Records", description = "독서 기록/통계 API | by 쏘이/김서연")
public interface ReadingSessionRecordsControllerDocs {

    @Operation(
            summary = "월별 독서 기록 조회",
            description = "해당 년/월의 모든 독서 세션 기록을 조회합니다."
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
                                        "records": [
                                          {
                                            "day": 1,
                                            "start_time": "14:00",
                                            "end_time": "14:45",
                                            "total_minutes": 45
                                          },
                                          {
                                            "day": 1,
                                            "start_time": "20:00",
                                            "end_time": "20:30",
                                            "total_minutes": 30
                                          },
                                          {
                                            "day": 3,
                                            "start_time": "10:00",
                                            "end_time": "11:20",
                                            "total_minutes": 80
                                          }
                                        ]
                                      }
                                    }
                                    """)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 파라미터 (year: 2000~2100, month: 1~12)",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {
                                      "is_success": false,
                                      "code": 400,
                                      "message": "month: 1에서 12 사이여야 합니다."
                                    }
                                    """)
                    )
            )
    })
    ResponseEntity<ApiResponse<ReadingSessionResponse.MonthlyRecords>> getMonthlyRecords(
            @Parameter(
                    description = "년도 (2000~2100)",
                    required = true,
                    example = "2026",
                    schema = @Schema(minimum = "2000", maximum = "2100")
            )
            Integer year,
            @Parameter(
                    description = "월 (1~12)",
                    required = true,
                    example = "2",
                    schema = @Schema(minimum = "1", maximum = "12")
            )
            Integer month
    );
}
