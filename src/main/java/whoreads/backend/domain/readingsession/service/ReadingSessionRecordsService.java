package whoreads.backend.domain.readingsession.service;

import whoreads.backend.domain.readingsession.dto.ReadingSessionResponse;

public interface ReadingSessionRecordsService {

    ReadingSessionResponse.MonthlyRecords getMonthlyRecords(Integer year, Integer month);
}
