package whoreads.backend.domain.readingsession.controller;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import whoreads.backend.domain.readingsession.controller.docs.ReadingSessionRecordsControllerDocs;
import whoreads.backend.domain.readingsession.dto.ReadingSessionResponse;
import whoreads.backend.domain.readingsession.service.ReadingSessionRecordsService;
import whoreads.backend.global.exception.CustomException;
import whoreads.backend.global.exception.ErrorCode;
import whoreads.backend.global.response.ApiResponse;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/me/reading-sessions/records")
public class ReadingSessionRecordsController implements ReadingSessionRecordsControllerDocs {

    private final ReadingSessionRecordsService readingSessionRecordsService;

    @Override
    @GetMapping("/monthly")
    public ResponseEntity<ApiResponse<ReadingSessionResponse.MonthlyRecords>> getMonthlyRecords(
            @AuthenticationPrincipal Long memberId,
            @RequestParam @Min(2000) @Max(2100) Integer year,
            @RequestParam @Min(1) @Max(12) Integer month
    ) {
        validateAuthentication(memberId);
        ReadingSessionResponse.MonthlyRecords result = readingSessionRecordsService.getMonthlyRecords(memberId, year, month);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    private void validateAuthentication(Long memberId) {
        if (memberId == null) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }
    }
}
