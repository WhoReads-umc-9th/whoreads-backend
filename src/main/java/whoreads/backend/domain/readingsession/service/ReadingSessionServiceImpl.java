package whoreads.backend.domain.readingsession.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import whoreads.backend.domain.focusmode.entity.FocusTimerSetting;
import whoreads.backend.domain.focusmode.repository.FocusModeRepository;
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
    private final FocusModeRepository focusModeRepository;

    @Override
    public ReadingSessionResponse.StartResult startSession(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        // 이미 진행 중이거나 일시정지된 세션이 있는지 확인
        // SUSPENDED 세션은 의도적으로 제외: 자동 중단된 세션이 새 세션 시작을 막지 않도록 함
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

    @Override
    public void heartbeat(Long sessionId, Long memberId) {
        ReadingSession session = findByIdAndValidateOwnership(sessionId, memberId);
        session.updateHeartbeat(LocalDateTime.now());
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

    public ReadingSessionResponse.IncompleteResult getIncompleteSession(Long memberId) {
        ReadingSession session = readingSessionRepository.findByMemberIdAndStatusIn(memberId,
                List.of(SessionStatus.IN_PROGRESS, SessionStatus.PAUSED, SessionStatus.SUSPENDED)).orElse(null);

        if (session == null)
            throw new CustomException(ErrorCode.INCOMPLETE_SESSION_NOT_FOUND);

        FocusTimerSetting focusSetting = focusModeRepository.findByMemberId(memberId)
                .orElseGet(() -> FocusTimerSetting.builder()
                        .timerMinutes(10000L) // (선택사항) 타이머 기본값 세팅
                        .build());

        long totalReadMinutes = session.calculateTotalMinutes();

        if (session.getStatus() == SessionStatus.IN_PROGRESS) {
            for (ReadingInterval interval : session.getIntervals()) {
                if (interval.getEndTime() == null) {
                    long currentDiff = java.time.temporal.ChronoUnit.MINUTES.between(
                            interval.getStartTime(),
                            java.time.LocalDateTime.now()
                    );
                    totalReadMinutes += currentDiff;
                    break;
                }
            }
        }

        // 목표 시간(임시 60분) 기반 남은 시간 계산
        long targetMinutes = 60L;   // 이 부분 사용자가 실제로 설정한 시간으로 받아와야됨. FocusTimerSetting 부분
        long remainingMinutes = Math.max(0, targetMinutes - totalReadMinutes);

        // 4. 공통 반환 필드 세팅 (status에 실제 DB 상태값 반영)
        ReadingSessionResponse.IncompleteResult.IncompleteResultBuilder builder =
                ReadingSessionResponse.IncompleteResult.builder()
                        .sessionId(session.getId())
                        .status(session.getStatus().name()) // 오타 수정: 실제 상태값 반환
                        .totalReadMinutes(totalReadMinutes)
                        .focusBlockEnabled(focusSetting.getFocusBlockEnabled())
                        .whiteNoiseEnabled(focusSetting.getWhiteNoiseEnabled());

        // 5. 실제 상태에 따른 추가 필드 조립
        if (session.getStatus() == SessionStatus.IN_PROGRESS) {
            long idleMinutes = java.time.temporal.ChronoUnit.MINUTES.between(
                    session.getUpdatedAt(),
                    java.time.LocalDateTime.now()
            );
            builder.idleMinutes(idleMinutes)
                    .remainingMinutes(remainingMinutes);

        } else if (session.getStatus() == SessionStatus.PAUSED) {
            builder.remainingMinutes(remainingMinutes);
        }

        return builder.build();
    }
}
