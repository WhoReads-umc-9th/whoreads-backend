package whoreads.backend.domain.readingsession.controller.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import whoreads.backend.domain.readingsession.dto.ReadingSessionRequest;
import whoreads.backend.domain.readingsession.dto.ReadingSessionResponse;
import whoreads.backend.global.response.ApiResponse;

@Tag(name = "Timer", description = "독서 타이머 API | by 쏘이/김서연")
public interface ReadingSessionControllerDocs {

    @Operation(
            summary = "독서 세션 시작",
            description = "새로운 독서 세션을 시작합니다. 첫 번째 인터벌이 자동으로 생성됩니다. "
                    + "일시정지·중단된 채 남은 세션은 마지막으로 읽은 시점까지의 시간으로 완료 처리되며, 진행 중인 세션이 있으면 409를 반환합니다.\n\n"
                    + "요청 body로 세션의 타이머 주기(분 단위)를 지정할 수 있습니다. 세 값 모두 선택이며, 보내지 않거나 body를 생략하면 기본값이 적용됩니다.\n"
                    + "- heartbeat_interval_minutes (기본 1): 프론트가 heartbeat를 보내는 주기\n"
                    + "- suspend_after_minutes (기본 5): 마지막 heartbeat 이후 이 시간이 지나면 진행 중인 세션을 중단(SUSPENDED) 처리. heartbeat_interval_minutes 이상이어야 합니다.\n"
                    + "- complete_after_minutes (기본 120): 일시정지 후 이 시간이 지나면 세션을 자동 완료 처리하고, 중단된 세션은 이 시간이 지나면 이어 읽을 수 없습니다.\n"
                    + "각 값은 1~1440 범위이며 heartbeat_interval_minutes ≤ suspend_after_minutes ≤ complete_after_minutes 순서를 지켜야 합니다(보내지 않은 값은 기본값 기준으로 비교). 어기면 400을 반환합니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = false,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {
                                      "heartbeat_interval_minutes": 5,
                                      "suspend_after_minutes": 10,
                                      "complete_after_minutes": 120
                                    }
                                    """)
                    )
            )
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "세션 시작 성공",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {
                                      "is_success": true,
                                      "code": 201,
                                      "message": "독서 세션을 시작했습니다.",
                                      "result": {
                                        "session_id": 1
                                      }
                                    }
                                    """)
                    )
            )
    })
    ResponseEntity<ApiResponse<ReadingSessionResponse.StartResult>> startSession(
            @AuthenticationPrincipal Long memberId,
            ReadingSessionRequest.Start request
    );

    @Operation(
            summary = "독서 세션 일시정지",
            description = "진행 중인 독서 세션을 일시정지합니다. 현재 인터벌이 종료됩니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "일시정지 성공",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {
                                      "is_success": true,
                                      "code": 200,
                                      "message": "독서 세션을 일시정지했습니다."
                                    }
                                    """)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "세션을 찾을 수 없음",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {
                                      "is_success": false,
                                      "code": 404,
                                      "message": "세션을 찾을 수 없습니다."
                                    }
                                    """)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 세션 상태",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {
                                      "is_success": false,
                                      "code": 400,
                                      "message": "진행 중인 세션만 일시정지할 수 있습니다."
                                    }
                                    """)
                    )
            )
    })
    ResponseEntity<ApiResponse<Void>> pauseSession(
            @Parameter(description = "세션 ID", required = true)
            Long sessionId,
            @AuthenticationPrincipal Long memberId
    );

    @Operation(
            summary = "독서 세션 재개",
            description = "일시정지된 독서 세션을 재개합니다. 새로운 인터벌이 생성됩니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "재개 성공",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {
                                      "is_success": true,
                                      "code": 200,
                                      "message": "독서 세션을 재개했습니다."
                                    }
                                    """)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "세션을 찾을 수 없음",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {
                                      "is_success": false,
                                      "code": 404,
                                      "message": "세션을 찾을 수 없습니다."
                                    }
                                    """)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 세션 상태",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {
                                      "is_success": false,
                                      "code": 400,
                                      "message": "일시정지된 세션만 재개할 수 있습니다."
                                    }
                                    """)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "완료되었거나 중단된 세션 (일시정지 후 complete_after_minutes가 지나 자동 완료된 세션 포함)",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {
                                      "is_success": false,
                                      "code": 409,
                                      "message": "중단된 세션이거나 완료된 세션입니다."
                                    }
                                    """)
                    )
            )
    })
    ResponseEntity<ApiResponse<Void>> resumeSession(
            @Parameter(description = "세션 ID", required = true)
            Long sessionId,
            @AuthenticationPrincipal Long memberId
    );

    @Operation(
            summary = "독서 세션 완료",
            description = "독서 세션을 완료합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "완료 성공",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {
                                      "is_success": true,
                                      "code": 200,
                                      "message": "독서 세션을 완료했습니다."
                                    }
                                    """)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "세션을 찾을 수 없음",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {
                                      "is_success": false,
                                      "code": 404,
                                      "message": "세션을 찾을 수 없습니다."
                                    }
                                    """)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 세션 상태 (이미 완료됨)",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {
                                      "is_success": false,
                                      "code": 400,
                                      "message": "이미 완료된 세션입니다."
                                    }
                                    """)
                    )
            )
    })
    ResponseEntity<ApiResponse<Void>> completeSession(
            @Parameter(description = "세션 ID", required = true)
            Long sessionId,
            @AuthenticationPrincipal Long memberId
    );

    @Operation(
            summary = "독서 세션 heartbeat",
            description = "앱이 살아있음을 서버에 알립니다. 세션 시작 시 전달한 heartbeat_interval_minutes(기본 1분)마다 호출하세요. "
                    + "진행 중(IN_PROGRESS) 세션이 suspend_after_minutes(기본 5분) 이상 heartbeat가 없으면 서버가 세션을 중단(SUSPENDED) 처리하며, "
                    + "마지막 heartbeat 후 complete_after_minutes(기본 120분) 안에는 recover로 이어할 수 있습니다. "
                    + "진행 중(IN_PROGRESS)·일시정지(PAUSED) 세션만 받으며, 완료/중단된 세션이면 409를 반환하므로 heartbeat를 멈추세요."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "heartbeat 갱신 성공",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {
                                      "is_success": true,
                                      "code": 200,
                                      "message": "세션 heartbeat 정보를 전송했습니다."
                                    }
                                    """)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "완료/중단된 세션",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {
                                      "is_success": false,
                                      "code": 409,
                                      "message": "중단된 세션이거나 완료된 세션입니다."
                                    }
                                    """)
                    )
            )
    })
    ResponseEntity<ApiResponse<Void>> heartbeat(
            @Parameter(description = "세션 ID", required = true)
            Long sessionId,
            @AuthenticationPrincipal Long memberId
    );

    @Operation(summary = "미완료 독서 세션 조회", description = "사용자가 이전에 종료하지 않은 독서 세션(IN_PROGRESS, PAUSED, SUSPENDED)이 있는지 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "미완료 세션 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "미완료된 독서 세션이 존재하지 않습니다.")
    })
    ApiResponse<ReadingSessionResponse.IncompleteResult> incompleteSession(Long memberId);

    @Operation(
            summary = "중단된 독서 세션 재개",
            description = """
                    미완료 독서 세션(IN_PROGRESS, PAUSED, SUSPENDED)을 재개하고 남은 타이머 시간을 반환합니다.
                    기존 구간을 마지막 하트비트 시점에 닫고, 남은 시간이 있으면 현재 시각부터 새 구간을 시작합니다.
                    남은 시간이 없으면 세션을 완료 처리합니다.
                    세션 상태를 변경하므로 POST로 호출해야 합니다. 상태 조회만 필요하면 `GET /api/reading-sessions/incomplete`를 사용하세요.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "요청이 성공했습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "마지막 활동으로부터 허용 시간(complete_after_minutes, 기본 120분)이 경과하여 해당 세션을 재개할 수 없습니다. (SESSION_EXPIRED)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "독서세션을 찾을 수 없습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "완료된 세션입니다. (SESSION_NOT_ACTIVE)")
    })
    ApiResponse<ReadingSessionResponse.ResumeResult> recoverSession(Long sessionId, Long memberId);

    @Operation(
            summary = "[Deprecated] 중단된 독서 세션 재개 (GET)",
            description = "구버전 앱 호환용입니다. `POST /api/reading-sessions/{sessionId}/recover`를 사용하세요. 동작과 응답은 POST 경로와 같습니다.",
            deprecated = true
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "요청이 성공했습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "마지막 활동으로부터 허용 시간(complete_after_minutes, 기본 120분)이 경과하여 해당 세션을 재개할 수 없습니다. (SESSION_EXPIRED)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "독서세션을 찾을 수 없습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "완료된 세션입니다. (SESSION_NOT_ACTIVE)")
    })
    @Deprecated
    ApiResponse<ReadingSessionResponse.ResumeResult> recoverSessionLegacy(Long sessionId, Long memberId);

    @Operation(
            summary = "중단된 시간 자동 정산 및 세션 완료",
            description = "마지막 하트비트 이후부터 현재까지의 중단된 시간을 독서 시간으로  인정하고 세션을 종료합니다. "
                    + "진행 중인 구간이 있으면 그 구간을 현재 시각까지 독서 시간으로 인정합니다. "
                    + "공백은 타이머 목표 시간까지만 채우며(목표 미설정 시 제한 없음), 일시정지 세션은 일시정지 시간을 더하지 않고 완료합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "요청이 성공했습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "이미 완료된 세션입니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "본인의 세션이 아닙니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "독서세션을 찾을 수 없습니다.")
    })
    public ApiResponse<Void> resolveIdleTime(Long sessionId, Long memberId);
}
