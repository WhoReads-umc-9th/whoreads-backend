package whoreads.backend.domain.readingsession.service;

import whoreads.backend.domain.readingsession.dto.ReadingSessionResponse;

public interface ReadingSessionService {

    ReadingSessionResponse.StartResult startSession(Long memberId);

    void pauseSession(Long sessionId, Long memberId);

    void resumeSession(Long sessionId, Long memberId);

    void completeSession(Long sessionId, Long memberId);
}
