package whoreads.backend.domain.notification.service;

import com.google.firebase.messaging.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import whoreads.backend.domain.member.repository.MemberRepository;
import whoreads.backend.domain.notification.dto.FcmMessageDTO;
import whoreads.backend.domain.notification.dto.MemberTokenDTO;
import whoreads.backend.global.exception.CustomException;
import whoreads.backend.global.exception.ErrorCode;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationPushServiceImpl implements NotificationPushService {

    private final FirebaseMessaging firebaseMessaging;
    private final MemberRepository memberRepository;
    private static final int FCM_BATCH_SIZE = 500;
    private final NotificationHistoryService notificationHistoryService;
    private final NotificationIdempotencyService notificationIdempotencyService;
    private final NotificationTokenService notificationTokenService;

    @Async("WhoReadsAsyncExecutor")
    @Transactional
    // 테스트용 알림 1개 발송 메서드
    public void sendNotification(String fcmToken, FcmMessageDTO dto) {
        send(createMessage(fcmToken, dto), fcmToken);
    }

    private void send(Message message, String fcmToken) {
        try {
            firebaseMessaging.send(message);
        } catch (FirebaseMessagingException e) {
            MessagingErrorCode errorCode = e.getMessagingErrorCode();

            // 유효하지 않은 토큰인 경우 즉시 삭제
            if (errorCode == MessagingErrorCode.UNREGISTERED ||
                errorCode == MessagingErrorCode.INVALID_ARGUMENT) {
                memberRepository.clearToken(fcmToken);
                throw new CustomException(ErrorCode.FCM_TOKEN_UNREGISTERED);
            }
            // 그 외의 에러 예외 처리
            throw new CustomException(ErrorCode.FCM_SEND_FAILED);
        }
    }

    private Message createMessage(String fcmToken, FcmMessageDTO dto) {
        return Message.builder()
                .setToken(fcmToken)
                // 알림 설정 ( 포그라운드 노출용 )
                .setNotification(Notification.builder()
                        .setTitle(dto.getTitle())
                        .setBody(dto.getBody())
                        .build())
                // 안드로이드 설정 추가
                .setAndroidConfig(AndroidConfig.builder()
                        .setPriority(AndroidConfig.Priority.HIGH) // 즉시 발송
                        .setNotification(AndroidNotification.builder()
                                .setChannelId("high_importance_channel") // 프런트와 일치 필수
                                .setIcon("app_logo") // 💡 사진에 올린 파일명 (확장자 제외)
                                .setClickAction("FLUTTER_NOTIFICATION_CLICK") // 백그라운드 클릭 핸들링용
                                .build())
                        .build())
                // 커스텀 데이터 ( 프론트 확인용 )
                .putData("title", dto.getTitle())
                .putData("body", dto.getBody())
                .putData("type",dto.getType())
                .build();
    }
    @Override
    public void sendMulticast(
            List<MemberTokenDTO> memberTokens,
            FcmMessageDTO dto,
            String idempotencyKey,
            Duration ttl
    ) {
        if (memberTokens == null || memberTokens.isEmpty()) {
            return;
        }

        for (int i = 0; i < memberTokens.size(); i += FCM_BATCH_SIZE) {

            List<MemberTokenDTO> subList = memberTokens.subList(
                    i,
                    Math.min(i + FCM_BATCH_SIZE, memberTokens.size())
            );

            List<Message> messages = new ArrayList<>();
            List<String> acquiredKeys = new ArrayList<>();
            List<String> acquiredTokens = new ArrayList<>();

            for (MemberTokenDTO target : subList) {

                Long memberId = target.getMemberId();
                String key = buildKey(memberId, idempotencyKey);

                if (target.getFcmToken() == null
                        || target.getFcmToken().isBlank()) {
                    log.warn("[FCM] 토큰 없음. memberId={}", memberId);
                    continue;
                }

                try {
                    boolean acquired = notificationIdempotencyService.acquire(key, ttl);

                    if (!acquired) {
                        log.info("[FCM] 중복 알림 차단. key={}", key);
                        continue;
                    }
                } catch (Exception e) {
                    // Redis 장애 시 중복 푸시를 피하기 위해 푸시는 보내지 않지만, 인앱 이력은 남긴다.
                    log.error("[FCM] 멱등키 선점 실패. 푸시 생략, 이력만 저장. key={}", key, e);
                    saveHistorySafely(memberId, dto);
                    continue;
                }

                try {
                    Long historyId =
                            notificationHistoryService.saveHistory(memberId, dto);

                    Map<String, String> data = new HashMap<>();
                    data.put("id", String.valueOf(historyId));
                    data.put("title", dto.getTitle());
                    data.put("body", dto.getBody());
                    data.put("type", dto.getType());

                    if (dto.getCelebrityId() != null
                            && dto.getBookId() != null) {
                        data.put(
                                "celebrity_id",
                                dto.getCelebrityId().toString()
                        );
                        data.put("book_id", dto.getBookId().toString());
                    }

                    Message message = Message.builder()
                            .setToken(target.getFcmToken())
                            .setNotification(
                                    Notification.builder()
                                            .setTitle(dto.getTitle())
                                            .setBody(dto.getBody())
                                            .build()
                            )
                            .setAndroidConfig(
                                    AndroidConfig.builder()
                                            .setPriority(
                                                    AndroidConfig.Priority.HIGH
                                            )
                                            .setNotification(
                                                    AndroidNotification.builder()
                                                            .setChannelId(
                                                                    "high_importance_channel"
                                                            )
                                                            .setIcon("app_logo")
                                                            .setClickAction(
                                                                    "FLUTTER_NOTIFICATION_CLICK"
                                                            )
                                                            .build()
                                            )
                                            .build()
                            )
                            .putAllData(data)
                            .build();

                    messages.add(message);
                    acquiredKeys.add(key);
                    acquiredTokens.add(target.getFcmToken());

                } catch (Exception e) {
                    log.error(
                            "[FCM] 히스토리 저장 또는 메시지 생성 실패. key={}",
                            key,
                            e
                    );
                    markFailedSafely(key, e.getMessage());
                }
            }

            if (messages.isEmpty()) {
                continue;
            }

            try {
                BatchResponse response = firebaseMessaging.sendEach(messages);

                for (int j = 0; j < response.getResponses().size(); j++) {

                    SendResponse result = response.getResponses().get(j);
                    String key = acquiredKeys.get(j);

                    if (result.isSuccessful()) {
                        markSentSafely(key, ttl);
                    } else {
                        FirebaseMessagingException error =
                                result.getException();

                        // 만료·삭제된 토큰은 단건 발송과 같이 즉시 해제 (그대로 두면 30일 정리 전까지 계속 실패)
                        if (error != null
                                && error.getMessagingErrorCode() == MessagingErrorCode.UNREGISTERED) {
                            clearInvalidTokenSafely(acquiredTokens.get(j));
                        }

                        markFailedSafely(
                                key,
                                error == null
                                        ? "Unknown FCM error"
                                        : error.getMessage()
                        );
                    }
                }

                log.info(
                        "[FCM] 배치 완료. 성공={}, 실패={}",
                        response.getSuccessCount(),
                        response.getFailureCount()
                );

            } catch (Exception e) {
                // 요청이 FCM에 도달했는지 알 수 없으므로 FAILED(재시도 가능)로 바꾸지 않는다.
                // PROCESSING 임대가 만료된 뒤에만 재선점할 수 있어, 이미 발송됐을 때의 중복을 줄인다.
                log.error("[FCM] 배치 호출 실패. 발송 여부 불확실, 임대 만료까지 재선점 차단. keys={}", acquiredKeys, e);
            }
        }
    }

    private String buildKey(Long memberId, String eventKey) {
        String[] parts = eventKey.split(":", 2);

        if (parts.length != 2) {
            throw new IllegalArgumentException(
                    "Invalid notification idempotency key"
            );
        }

        return parts[0] + ":" + memberId + ":" + parts[1];
    }

    private void clearInvalidTokenSafely(String fcmToken) {
        try {
            notificationTokenService.clearInvalidToken(fcmToken);
        } catch (Exception e) {
            log.error("[FCM] 유효하지 않은 토큰 해제 실패", e);
        }
    }

    private void saveHistorySafely(Long memberId, FcmMessageDTO dto) {
        try {
            notificationHistoryService.saveHistory(memberId, dto);
        } catch (Exception e) {
            log.error("[FCM] 인앱 이력 저장 실패. memberId={}", memberId, e);
        }
    }

    private void markSentSafely(String key, Duration ttl) {
        try {
            notificationIdempotencyService.markSent(key, ttl);
        } catch (Exception e) {
            log.error("[FCM] Redis SENT 기록 실패. key={}", key, e);
        }
    }

    private void markFailedSafely(String key, String reason) {
        try {
            notificationIdempotencyService.markFailed(key, reason);
        } catch (Exception e) {
            log.error("[FCM] Redis FAILED 기록 실패. key={}", key, e);
        }
    }
}