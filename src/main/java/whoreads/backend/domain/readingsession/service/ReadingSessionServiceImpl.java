package whoreads.backend.domain.readingsession.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import whoreads.backend.domain.focusmode.entity.FocusTimerSetting;
import whoreads.backend.domain.focusmode.repository.FocusModeRepository;
import whoreads.backend.domain.member.entity.Member;
import whoreads.backend.domain.member.repository.MemberRepository;
import whoreads.backend.domain.readingsession.dto.ReadingSessionRequest;
import whoreads.backend.domain.readingsession.dto.ReadingSessionResponse;
import whoreads.backend.domain.readingsession.entity.ReadingInterval;
import whoreads.backend.domain.readingsession.entity.ReadingSession;
import whoreads.backend.domain.readingsession.enums.SessionStatus;
import whoreads.backend.domain.readingsession.repository.ReadingSessionRepository;
import whoreads.backend.global.exception.CustomException;
import whoreads.backend.global.exception.ErrorCode;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

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

        // 완료/중단된 세션의 heartbeat는 거절해 앱이 heartbeat를 멈추거나 복구 흐름으로 넘어가게 함
        if (session.getStatus() != SessionStatus.IN_PROGRESS && session.getStatus() != SessionStatus.PAUSED) {
            throw new CustomException(ErrorCode.SESSION_NOT_ACTIVE);
        }

        LocalDateTime now = LocalDateTime.now();
        session.updateHeartbeat(now);

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

    @Override
    public ReadingSessionResponse.ResumeResult resumeIncompleteSession(Long sessionId, Long memberId) {
        ReadingSession session = findByIdAndValidateOwnership(sessionId, memberId);
        LocalDateTime now = LocalDateTime.now();

        // 마지막 하트비트 시간 확인
        LocalDateTime lastHeartbeat = session.getLastHeartbeatAt();

        // 마지막 하트비트로부터 2시간을 초과했는지 확인
        long minutesSinceLastHeartbeat = java.time.Duration.between(lastHeartbeat, now).toMinutes();
        if (minutesSinceLastHeartbeat >= 120) {
            FocusTimerSetting focusSetting = focusModeRepository.findByMemberId(memberId)
                    .orElseGet(() -> FocusTimerSetting.builder().build());
            session.suspend(focusSetting.getTimerMinutes());

            throw new CustomException(ErrorCode.SESSION_EXPIRED);
        }

        // 지금까지의 세션 시간 및 남은 세션 시간 계산
        FocusTimerSetting focusSetting = focusModeRepository.findByMemberId(memberId)
                .orElseGet(() -> FocusTimerSetting.builder().build());

        // 상태와 상관없이 일단 기존 인터벌들을 마지막 하트비트 시점에 다 닫아버림
        // 이앱이 꺼져있던 공백 시간이 독서 시간으로 잡히지 않는다.
        if (session.getStatus() != SessionStatus.SUSPENDED) {
            session.suspend(focusSetting.getTimerMinutes());
        }

        long totalReadMinutes = session.calculateTotalMinutes();
        long remainingMinutes = focusSetting.getTimerMinutes() - totalReadMinutes;

        // 남은 시간이 있는 경우 '지금 시각'에 시작하는 새로운 ReadingInterval 생성
        if (remainingMinutes > 0) {
            session.recover();

            ReadingInterval newInterval = ReadingInterval.builder()
                    .readingSession(session)
                    .startTime(now)
                    .build();
            session.addInterval(newInterval);

            // 하트비트 현재 시간으로 수정
            session.updateHeartbeat(now);
        } else
            session.complete();

        return ReadingSessionResponse.ResumeResult.builder()
                .remainingMinutes(remainingMinutes)
                .build();
    }

    public void resolveIdleTime(Long sessionId, Long memberId) {
        ReadingSession session = findByIdAndValidateOwnership(sessionId, memberId);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lastHeartbeat = session.getLastHeartbeatAt();

        // 일시정지 중인 시간은 독서 시간이 아니므로 공백을 채우지 않고 완료
        if (session.getStatus() == SessionStatus.PAUSED) {
            session.complete();
            return;
        }

        // 공백은 타이머 목표 시간까지만 채움 (목표를 설정하지 않았으면 제한 없음)
        long goalMinutes = focusModeRepository.findByMemberId(memberId)
                .map(FocusTimerSetting::getTimerMinutes)
                .orElse(0L);

        // 열린 구간이 있으면(IN_PROGRESS) 공백 시간까지 포함해 닫음
        // 공백 구간을 따로 추가하면 마지막 하트비트 이후 시간이 중복 집계되므로 추가하지 않는다
        Optional<ReadingInterval> activeInterval = session.getIntervals().stream()
                .filter(interval -> interval.getEndTime() == null)
                .findFirst();
        if (activeInterval.isPresent()) {
            ReadingInterval interval = activeInterval.get();
            // 마지막 하트비트까지는 확인된 독서 시간이므로 목표 시간과 관계없이 인정
            LocalDateTime confirmedEnd = lastHeartbeat == null || lastHeartbeat.isBefore(interval.getStartTime())
                    ? interval.getStartTime()
                    : (lastHeartbeat.isAfter(now) ? now : lastHeartbeat);
            interval.end(fillUntil(session, confirmedEnd, now, goalMinutes));
            session.complete();
            return;
        }

        // 마지막 하트비트 시점부터 현재 사이의 공백을 메워줌
        if (lastHeartbeat != null) {
            // 일시정지 후 재개하고 첫 하트비트 전에 중단되면 하트비트가 일시정지 이전 시각일 수 있으므로,
            // 마지막으로 닫힌 구간의 종료 시각보다 앞서서 공백을 채우지 않는다
            LocalDateTime idleStart = session.getIntervals().stream()
                    .map(ReadingInterval::getEndTime)
                    .filter(Objects::nonNull)
                    .max(Comparator.naturalOrder())
                    .filter(end -> end.isAfter(lastHeartbeat))
                    .orElse(lastHeartbeat);

            if (idleStart.isBefore(now)) {
                LocalDateTime idleEnd = fillUntil(session, idleStart, now, goalMinutes);

                if (idleEnd.isAfter(idleStart)) {
                    ReadingInterval idleInterval = ReadingInterval.builder()
                            .startTime(idleStart)
                            .readingSession(session)
                            .build();

                    idleInterval.end(idleEnd);
                    session.addInterval(idleInterval);
                }
            }
        }

        session.complete();
    }

    // from~now 공백을 채우되, 세션 총 독서 시간이 타이머 목표 시간을 넘지 않는 시각까지만 채움
    // 열린 구간은 from까지 읽은 것으로 계산한다. 목표가 없으면(0분) now까지 채움
    private LocalDateTime fillUntil(ReadingSession session, LocalDateTime from, LocalDateTime now, long goalMinutes) {
        if (goalMinutes <= 0) {
            return now;
        }

        Duration read = session.getIntervals().stream()
                .map(interval -> Duration.between(interval.getStartTime(),
                        interval.getEndTime() != null ? interval.getEndTime() : from))
                .reduce(Duration.ZERO, Duration::plus);
        Duration remaining = Duration.ofMinutes(goalMinutes).minus(read);

        if (remaining.isNegative() || remaining.isZero()) {
            return from;
        }
        LocalDateTime capped = from.plus(remaining);
        return capped.isBefore(now) ? capped : now;
    }
}
