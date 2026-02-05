package whoreads.backend.domain.readingsession.service;

import whoreads.backend.domain.readingsession.dto.ReadingSessionResponse;

public interface ReadingSessionStatsService {

    ReadingSessionResponse.TodayFocus getTodayFocus();

    ReadingSessionResponse.TotalFocus getTotalFocus();
}
