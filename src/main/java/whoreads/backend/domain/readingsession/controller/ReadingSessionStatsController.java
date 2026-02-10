package whoreads.backend.domain.readingsession.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import whoreads.backend.domain.readingsession.controller.docs.ReadingSessionStatsControllerDocs;
import whoreads.backend.domain.readingsession.dto.ReadingSessionResponse;
import whoreads.backend.domain.readingsession.service.ReadingSessionStatsService;
import whoreads.backend.global.response.ApiResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/me/reading-sessions/stats")
public class ReadingSessionStatsController implements ReadingSessionStatsControllerDocs {

    private final ReadingSessionStatsService readingSessionStatsService;

    @Override
    @GetMapping("/today")
    public ResponseEntity<ApiResponse<ReadingSessionResponse.TodayFocus>> getTodayFocus(
            @AuthenticationPrincipal Long memberId
    ) {
        ReadingSessionResponse.TodayFocus result = readingSessionStatsService.getTodayFocus(memberId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @Override
    @GetMapping("/total")
    public ResponseEntity<ApiResponse<ReadingSessionResponse.TotalFocus>> getTotalFocus(
            @AuthenticationPrincipal Long memberId
    ) {
        ReadingSessionResponse.TotalFocus result = readingSessionStatsService.getTotalFocus(memberId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
