package whoreads.backend.domain.readingsession.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import whoreads.backend.domain.readingsession.dto.ReadingSessionResponse;
import whoreads.backend.domain.readingsession.enums.SessionStatus;
import whoreads.backend.domain.readingsession.repository.ReadingSessionRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReadingSessionStatsServiceImpl implements ReadingSessionStatsService {

    private final ReadingSessionRepository readingSessionRepository;

    @Override
    public ReadingSessionResponse.TodayFocus getTodayFocus(Long memberId) {
        LocalDate today = LocalDate.now();
        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime tomorrowStart = today.plusDays(1).atStartOfDay();
        LocalDateTime yesterdayStart = today.minusDays(1).atStartOfDay();

        Long todayMinutes = readingSessionRepository.sumTotalMinutesByMemberIdAndFinishedAtBetween(
                memberId, todayStart, tomorrowStart);
        Long yesterdayMinutes = readingSessionRepository.sumTotalMinutesByMemberIdAndFinishedAtBetween(
                memberId, yesterdayStart, todayStart);

        return ReadingSessionResponse.TodayFocus.builder()
                .todayMinutes(todayMinutes)
                .differenceFromYesterday(todayMinutes - yesterdayMinutes)
                .build();
    }

    @Override
    public ReadingSessionResponse.TotalFocus getTotalFocus(Long memberId) {
        Long totalMinutes = readingSessionRepository.sumTotalMinutesByMemberIdAndStatus(
                memberId, SessionStatus.COMPLETED);

        return ReadingSessionResponse.TotalFocus.builder()
                .totalMinutes(totalMinutes)
                .build();
    }
}
