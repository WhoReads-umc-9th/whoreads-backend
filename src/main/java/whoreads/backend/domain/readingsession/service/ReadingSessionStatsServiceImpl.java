package whoreads.backend.domain.readingsession.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import whoreads.backend.domain.readingsession.dto.ReadingSessionResponse;
import whoreads.backend.domain.readingsession.repository.ReadingSessionRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReadingSessionStatsServiceImpl implements ReadingSessionStatsService {

    private final ReadingSessionRepository readingSessionRepository;

    @Override
    public ReadingSessionResponse.TodayFocus getTodayFocus(Long memberId) {
        // TODO: 실제 구현 시
        // 1. memberId로 사용자의 모든 완료된 인터벌 조회
        // 2. 오늘 날짜에 해당하는 시간만 합산 (날짜 경계 분할 계산)
        // 3. 어제 날짜에 해당하는 시간도 합산
        // 4. 차이 계산

        // Mock: 오늘 45분, 어제 30분 → 차이 +15분
        return ReadingSessionResponse.TodayFocus.builder()
                .todayMinutes(45L)
                .differenceFromYesterday(15L)
                .build();
    }

    @Override
    public ReadingSessionResponse.TotalFocus getTotalFocus(Long memberId) {
        // TODO: 실제 구현 시
        // 1. memberId로 사용자의 모든 완료된 세션의 totalMinutes 합산

        // Mock: 총 1234분
        return ReadingSessionResponse.TotalFocus.builder()
                .totalMinutes(1234L)
                .build();
    }
}
