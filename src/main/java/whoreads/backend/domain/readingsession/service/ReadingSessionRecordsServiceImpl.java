package whoreads.backend.domain.readingsession.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import whoreads.backend.domain.readingsession.dto.ReadingSessionResponse;
import whoreads.backend.domain.readingsession.repository.ReadingSessionRepository;

import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReadingSessionRecordsServiceImpl implements ReadingSessionRecordsService {

    private final ReadingSessionRepository readingSessionRepository;

    @Override
    public ReadingSessionResponse.MonthlyRecords getMonthlyRecords(Integer year, Integer month) {
        // TODO: 실제 구현 시
        // 1. 로그인한 사용자의 해당 년/월 완료된 세션 조회
        // 2. 각 세션의 시작시간, 종료시간, 총 집중시간 반환

        // Mock 데이터
        List<ReadingSessionResponse.DailyRecord> records = List.of(
                ReadingSessionResponse.DailyRecord.builder()
                        .day(1)
                        .startTime(LocalTime.of(14, 0))
                        .endTime(LocalTime.of(14, 45))
                        .totalMinutes(45)
                        .build(),
                ReadingSessionResponse.DailyRecord.builder()
                        .day(1)
                        .startTime(LocalTime.of(20, 0))
                        .endTime(LocalTime.of(20, 30))
                        .totalMinutes(30)
                        .build(),
                ReadingSessionResponse.DailyRecord.builder()
                        .day(3)
                        .startTime(LocalTime.of(10, 0))
                        .endTime(LocalTime.of(11, 20))
                        .totalMinutes(80)
                        .build()
        );

        return ReadingSessionResponse.MonthlyRecords.builder()
                .records(records)
                .build();
    }
}
