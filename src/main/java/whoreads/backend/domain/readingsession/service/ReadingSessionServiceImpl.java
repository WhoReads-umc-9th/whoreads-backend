package whoreads.backend.domain.readingsession.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
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
        readingSessionRepository.findFirstByMemberIdAndStatusInOrderByCreatedAtDesc(
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

        LocalDateTime now = LocalDateTime.now();

        log.info("[세션 ID: {}] 하트비트 업데이트 시간: {}", sessionId, now);
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

    @Override
    public ReadingSessionResponse.IncompleteResult getIncompleteSession(Long memberId) {
        ReadingSession session = readingSessionRepository.findFirstByMemberIdAndStatusInOrderByCreatedAtDesc(memberId,
                List.of(SessionStatus.IN_PROGRESS, SessionStatus.PAUSED, SessionStatus.SUSPENDED)).orElse(null);

        if (session == null)
            throw new CustomException(ErrorCode.INCOMPLETE_SESSION_NOT_FOUND);

        FocusTimerSetting focusSetting = focusModeRepository.findByMemberId(memberId)
                .orElseGet(() -> FocusTimerSetting.builder().build());

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

        long targetMinutes = focusSetting.getTimerMinutes();
        long remainingMinutes = Math.max(0, targetMinutes - totalReadMinutes);

        // 4. 공통 반환 필드 세팅 (status에 실제 DB 상태값 반영)
        ReadingSessionResponse.IncompleteResult.IncompleteResultBuilder builder =
                ReadingSessionResponse.IncompleteResult.builder()
                        .sessionId(session.getId())
                        .status(session.getStatus().name())
                        .totalReadMinutes(totalReadMinutes)
                        .remainingMinutes(remainingMinutes)
                        .focusBlockEnabled(focusSetting.getFocusBlockEnabled())
                        .whiteNoiseEnabled(focusSetting.getWhiteNoiseEnabled());

        // 모든 상태에서 idle_minutes 계산
        // 마지막 하트비트 시간을 기준으로 하되, 한 번도 하트비트가 없었다면 updatedAt이나 createdAt 사용 (Null 에러 방지)
        LocalDateTime lastActivityTime = session.getLastHeartbeatAt();
        if (lastActivityTime == null)
            lastActivityTime = session.getUpdatedAt() != null ? session.getUpdatedAt() : session.getCreatedAt();

        long idleMinutes = java.time.temporal.ChronoUnit.MINUTES.between(
                lastActivityTime,
                java.time.LocalDateTime.now()
        );

        // 계산된 idleMinutes를 무조건 builder에 포함
        builder.idleMinutes(idleMinutes);

        return builder.build();
    }
}
