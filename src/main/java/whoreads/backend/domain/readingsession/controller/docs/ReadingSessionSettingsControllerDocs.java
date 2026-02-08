package whoreads.backend.domain.readingsession.controller.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import whoreads.backend.domain.readingsession.dto.ReadingSessionRequest;
import whoreads.backend.domain.readingsession.dto.ReadingSessionResponse;
import whoreads.backend.global.response.ApiResponse;

@Tag(name = "Timer - Settings", description = "독서 타이머 설정 API | by 쏘이/김서연")
public interface ReadingSessionSettingsControllerDocs {

    @Operation(
            summary = "집중 차단 모드 설정 조회",
            description = "현재 집중 차단 모드 설정값을 조회합니다."
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
                                        "focus_block_enabled": false
                                      }
                                    }
                                    """)
                    )
            )
    })
    ResponseEntity<ApiResponse<ReadingSessionResponse.FocusBlockSetting>> getFocusBlockSetting(
            Long memberId
    );

    @Operation(
            summary = "집중 차단 모드 설정 변경",
            description = """
                    집중 차단 모드를 켜거나 끕니다. 타이머 시작 시 이 설정값이 적용됩니다.
                    - 토글 방식이 아닌 명시적 값을 받습니다. (네트워크 재시도 시 상태 불일치 방지)
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
                                      "message": "집중 차단 모드 설정이 변경되었습니다.",
                                      "result": {
                                        "focus_block_enabled": true
                                      }
                                    }
                                    """)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {
                                      "is_success": false,
                                      "code": 400,
                                      "message": "필수 필드가 누락되었습니다."
                                    }
                                    """)
                    )
            )
    })
    ResponseEntity<ApiResponse<ReadingSessionResponse.FocusBlockSetting>> updateFocusBlockSetting(
            Long memberId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "집중 차단 모드 설정",
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {
                                      "focus_block_enabled": true
                                    }
                                    """)
                    )
            )
            ReadingSessionRequest.UpdateFocusBlock request
    );

    @Operation(
            summary = "백색소음 설정 조회",
            description = "현재 백색소음 설정값을 조회합니다."
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
                                        "white_noise_enabled": false
                                      }
                                    }
                                    """)
                    )
            )
    })
    ResponseEntity<ApiResponse<ReadingSessionResponse.WhiteNoiseSetting>> getWhiteNoiseSetting(
            Long memberId
    );

    @Operation(
            summary = "백색소음 설정 변경",
            description = """
                    백색소음을 켜거나 끕니다. 타이머 시작 시 이 설정값이 적용됩니다.
                    - 토글 방식이 아닌 명시적 값을 받습니다. (네트워크 재시도 시 상태 불일치 방지)
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
                                      "message": "백색소음 설정이 변경되었습니다.",
                                      "result": {
                                        "white_noise_enabled": true
                                      }
                                    }
                                    """)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {
                                      "is_success": false,
                                      "code": 400,
                                      "message": "필수 필드가 누락되었습니다."
                                    }
                                    """)
                    )
            )
    })
    ResponseEntity<ApiResponse<ReadingSessionResponse.WhiteNoiseSetting>> updateWhiteNoiseSetting(
            Long memberId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "백색소음 설정",
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {
                                      "white_noise_enabled": true
                                    }
                                    """)
                    )
            )
            ReadingSessionRequest.UpdateWhiteNoise request
    );

    @Operation(
            summary = "백색소음 목록 조회",
            description = "사용 가능한 백색소음 목록을 조회합니다."
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
                                        "items": [
                                          {
                                            "id": 1,
                                            "name": "빗소리",
                                            "audio_url": "https://example.com/audio/rain.mp3"
                                          },
                                          {
                                            "id": 2,
                                            "name": "파도소리",
                                            "audio_url": "https://example.com/audio/wave.mp3"
                                          },
                                          {
                                            "id": 3,
                                            "name": "카페 소음",
                                            "audio_url": "https://example.com/audio/cafe.mp3"
                                          },
                                          {
                                            "id": 4,
                                            "name": "모닥불",
                                            "audio_url": "https://example.com/audio/fire.mp3"
                                          }
                                        ]
                                      }
                                    }
                                    """)
                    )
            )
    })
    ResponseEntity<ApiResponse<ReadingSessionResponse.WhiteNoiseList>> getWhiteNoiseList();

    @Operation(
            summary = "차단 앱 목록 조회",
            description = "사용자가 설정한 차단 앱 목록을 조회합니다."
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
                                        "blocked_apps": [
                                          {
                                            "bundle_id": "com.burbn.instagram",
                                            "name": "Instagram"
                                          },
                                          {
                                            "bundle_id": "com.google.ios.youtube",
                                            "name": "YouTube"
                                          }
                                        ]
                                      }
                                    }
                                    """)
                    )
            )
    })
    ResponseEntity<ApiResponse<ReadingSessionResponse.BlockedApps>> getBlockedApps(
            Long memberId
    );

    @Operation(
            summary = "차단 앱 목록 저장",
            description = """
                    차단할 앱 목록을 저장합니다.
                    - **전체 덮어쓰기**: 기존 목록을 완전히 교체합니다.
                    - 추가/삭제 시에도 전체 목록을 보내주세요.
                    - 빈 배열을 보내면 모든 차단 앱이 해제됩니다.
                    """
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "저장 성공",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {
                                      "is_success": true,
                                      "code": 200,
                                      "message": "차단 앱 목록이 저장되었습니다.",
                                      "result": {
                                        "blocked_apps": [
                                          {
                                            "bundle_id": "com.burbn.instagram",
                                            "name": "Instagram"
                                          },
                                          {
                                            "bundle_id": "com.google.ios.youtube",
                                            "name": "YouTube"
                                          }
                                        ]
                                      }
                                    }
                                    """)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {
                                      "is_success": false,
                                      "code": 400,
                                      "message": "잘못된 요청 형식입니다."
                                    }
                                    """)
                    )
            )
    })
    ResponseEntity<ApiResponse<ReadingSessionResponse.BlockedApps>> updateBlockedApps(
            Long memberId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "차단 앱 목록",
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {
                                      "blocked_apps": [
                                        {
                                          "bundle_id": "com.burbn.instagram",
                                          "name": "Instagram"
                                        },
                                        {
                                          "bundle_id": "com.google.ios.youtube",
                                          "name": "YouTube"
                                        }
                                      ]
                                    }
                                    """)
                    )
            )
            ReadingSessionRequest.UpdateBlockedApps request
    );
}
