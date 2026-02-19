package whoreads.backend.domain.readingsession.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import whoreads.backend.domain.readingsession.controller.docs.ReadingSessionControllerDocs;
import whoreads.backend.domain.readingsession.dto.ReadingSessionResponse;
import whoreads.backend.domain.readingsession.service.ReadingSessionService;
import whoreads.backend.global.exception.CustomException;
import whoreads.backend.global.exception.ErrorCode;
import whoreads.backend.global.response.ApiResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reading-sessions")
public class ReadingSessionController implements ReadingSessionControllerDocs {

    private final ReadingSessionService readingSessionService;

    @Override
    @PostMapping("/start")
    public ResponseEntity<ApiResponse<ReadingSessionResponse.StartResult>> startSession(
            @AuthenticationPrincipal Long memberId
    ) {
        validateAuthentication(memberId);
        ReadingSessionResponse.StartResult result = readingSessionService.startSession(memberId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("독서 세션을 시작했습니다.", result));
    }

    @Override
    @PostMapping("/{sessionId}/pause")
    public ResponseEntity<ApiResponse<Void>> pauseSession(
            @PathVariable Long sessionId,
            @AuthenticationPrincipal Long memberId
    ) {
        validateAuthentication(memberId);
        readingSessionService.pauseSession(sessionId, memberId);
        return ResponseEntity.ok(ApiResponse.success("독서 세션을 일시정지했습니다."));
    }

    @Override
    @PostMapping("/{sessionId}/resume")
    public ResponseEntity<ApiResponse<Void>> resumeSession(
            @PathVariable Long sessionId,
            @AuthenticationPrincipal Long memberId
    ) {
        validateAuthentication(memberId);
        readingSessionService.resumeSession(sessionId, memberId);
        return ResponseEntity.ok(ApiResponse.success("독서 세션을 재개했습니다."));
    }

    @Override
    @PostMapping("/{sessionId}/complete")
    public ResponseEntity<ApiResponse<Void>> completeSession(
            @PathVariable Long sessionId,
            @AuthenticationPrincipal Long memberId
    ) {
        validateAuthentication(memberId);
        readingSessionService.completeSession(sessionId, memberId);
        return ResponseEntity.ok(ApiResponse.success("독서 세션을 완료했습니다."));
    }

    private void validateAuthentication(Long memberId) {
        if (memberId == null) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }
    }
}
