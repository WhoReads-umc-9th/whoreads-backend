package whoreads.backend.domain.readingsession.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import whoreads.backend.domain.member.entity.Member;
import whoreads.backend.domain.member.repository.MemberRepository;
import whoreads.backend.domain.readingsession.dto.ReadingSessionResponse;
import whoreads.backend.domain.readingsession.entity.ReadingInterval;
import whoreads.backend.domain.readingsession.entity.ReadingSession;
import whoreads.backend.domain.readingsession.enums.SessionStatus;
import whoreads.backend.domain.readingsession.repository.ReadingSessionRepository;
import whoreads.backend.global.exception.CustomException;
import whoreads.backend.global.exception.ErrorCode;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ReadingSessionServiceImpl implements ReadingSessionService {

    private final ReadingSessionRepository readingSessionRepository;
    private final MemberRepository memberRepository;

    @Override
    public ReadingSessionResponse.StartResult startSession(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        // 이미 진행 중이거나 일시정지된 세션이 있는지 확인
        readingSessionRepository.findByMemberIdAndStatusIn(
                memberId, List.of(SessionStatus.IN_PROGRESS, SessionStatus.PAUSED)
        ).ifPresent(s -> {
            throw new CustomException(ErrorCode.SESSION_ALREADY_ACTIVE);
        });

        ReadingSession session = ReadingSession.builder()
                .member(member)
                .build();

        ReadingInterval interval = ReadingInterval.builder()
                .readingSession(session)
                .startTime(LocalDateTime.now())
                .build();
        session.addInterval(interval);

        readingSessionRepository.save(session);

        return ReadingSessionResponse.StartResult.builder()
                .sessionId(session.getId())
                .build();
    }

    @Override
    public void pauseSession(Long sessionId, Long memberId) {
        ReadingSession session = findByIdAndValidateOwnership(sessionId, memberId);

        // 현재 진행 중인 인터벌 종료
        endActiveInterval(session);

        // 세션 일시정지 (엔티티 도메인 로직에서 상태 검증)
        session.pause();
    }

    @Override
    public void resumeSession(Long sessionId, Long memberId) {
        ReadingSession session = findByIdAndValidateOwnership(sessionId, memberId);

        // 세션 재개 (엔티티 도메인 로직에서 상태 검증)
        session.resume();

        // 새 인터벌 시작
        ReadingInterval interval = ReadingInterval.builder()
                .readingSession(session)
                .startTime(LocalDateTime.now())
                .build();
        session.addInterval(interval);
    }

    @Override
    public void completeSession(Long sessionId, Long memberId) {
        ReadingSession session = findByIdAndValidateOwnership(sessionId, memberId);

        // 진행 중인 인터벌이 있으면 종료
        endActiveInterval(session);

        // 세션 완료 (엔티티 도메인 로직에서 totalMinutes 계산, finishedAt 설정)
        session.complete();
    }

    private ReadingSession findByIdAndValidateOwnership(Long sessionId, Long memberId) {
        ReadingSession session = readingSessionRepository.findById(sessionId)
                .orElseThrow(() -> new CustomException(ErrorCode.SESSION_NOT_FOUND));

        if (!session.getMember().getId().equals(memberId)) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        return session;
    }

    private void endActiveInterval(ReadingSession session) {
        session.getIntervals().stream()
                .filter(interval -> interval.getEndTime() == null)
                .findFirst()
                .ifPresent(interval -> interval.end(LocalDateTime.now()));
    }
}
