package whoreads.backend.domain.readingsession.service;

import whoreads.backend.domain.readingsession.dto.ReadingSessionResponse;

public interface ReadingSessionStatsService {

    ReadingSessionResponse.TodayFocus getTodayFocus(Long memberId);

    ReadingSessionResponse.TotalFocus getTotalFocus(Long memberId);
}
