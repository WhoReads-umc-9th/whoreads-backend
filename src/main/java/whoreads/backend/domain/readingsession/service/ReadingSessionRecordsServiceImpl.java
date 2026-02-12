package whoreads.backend.domain.readingsession.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import whoreads.backend.domain.readingsession.dto.ReadingSessionResponse;
import whoreads.backend.domain.readingsession.entity.ReadingSession;
import whoreads.backend.domain.readingsession.repository.ReadingSessionRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReadingSessionRecordsServiceImpl implements ReadingSessionRecordsService {

    private final ReadingSessionRepository readingSessionRepository;

    @Override
    public ReadingSessionResponse.MonthlyRecords getMonthlyRecords(Long memberId, Integer year, Integer month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDateTime monthStart = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime monthEnd = yearMonth.plusMonths(1).atDay(1).atStartOfDay();

        List<ReadingSession> sessions = readingSessionRepository
                .findCompletedByMemberIdAndFinishedAtBetween(memberId, monthStart, monthEnd);

        List<ReadingSessionResponse.DailyRecord> records = sessions.stream()
                .map(session -> ReadingSessionResponse.DailyRecord.builder()
                        .day(session.getCreatedAt().getDayOfMonth())
                        .startTime(session.getCreatedAt().toLocalTime())
                        .endTime(session.getFinishedAt().toLocalTime())
                        .totalMinutes(session.getTotalMinutes())
                        .build())
                .toList();

        return ReadingSessionResponse.MonthlyRecords.builder()
                .records(records)
                .build();
    }
}
