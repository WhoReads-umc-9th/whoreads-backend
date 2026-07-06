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
    
    // 팔로우나 루틴 알림처럼 대량 발송
    public void sendMulticast(List<MemberTokenDTO> memberTokens, FcmMessageDTO dto) {
        if (memberTokens == null || memberTokens.isEmpty()) return;

        // 한번에 FCM_BATCH_SIZE 만큼만 발송
        for (int i = 0; i < memberTokens.size(); i += FCM_BATCH_SIZE) {
            List<MemberTokenDTO> subList = memberTokens.subList(i, Math.min(i + FCM_BATCH_SIZE, memberTokens.size()));

            // 1. 발송할 대상들의 히스토리를 DB에 먼저 생성
            List<Long> historyIds = new ArrayList<>();
            List<MemberTokenDTO> validTargets = new ArrayList<>();

            for (MemberTokenDTO tokenDTO : subList) {
                try {
                    // DB에 먼저 저장 후 생성된 고유 ID(PK)를 반환받음
                    Long historyId = notificationHistoryService.saveHistory(tokenDTO.getMemberId(), dto);
                    historyIds.add(historyId);
                    validTargets.add(tokenDTO); // 저장이 성공한 타겟만 발송 리스트에 포함
                } catch (Exception e) {
                    log.warn("[History] memberId={} 히스토리 저장 실패로 인해 발송 대상에서 제외", tokenDTO.getMemberId(), e);
                }
            }

            if (validTargets.isEmpty()) continue;

            // 2. 모든 유저에게 공통으로 들어갈 데이터 Map 구성
            Map<String, String> commonData = new HashMap<>();
            commonData.put("title", dto.getTitle());
            commonData.put("body", dto.getBody());
            commonData.put("type", dto.getType());

            if (dto.getCelebrityId() != null && dto.getBookId() != null) {
                commonData.put("celebrity_id", String.valueOf(dto.getCelebrityId()));
                commonData.put("book_id", String.valueOf(dto.getBookId()));
            }

            // 공통 알림(Notification) 객체 정의
            Notification notification = Notification.builder()
                .setTitle(dto.getTitle())
                .setBody(dto.getBody())
                .build();

            // 공통 안드로이드 설정 정의
            AndroidConfig androidConfig = AndroidConfig.builder()
                .setPriority(AndroidConfig.Priority.HIGH)
                .setNotification(AndroidNotification.builder()
                    .setChannelId("high_importance_channel")
                    .setIcon("app_logo")
                    .setClickAction("FLUTTER_NOTIFICATION_CLICK")
                    .build())
                .build();

            // 3. 개별 데이터(History ID)와 토큰을 매핑하여 단일 Message 리스트 생성
            List<Message> messages = new ArrayList<>();
            for (int j = 0; j < validTargets.size(); j++) {
                MemberTokenDTO target = validTargets.get(j);
                Long historyId = historyIds.get(j);

                // 유저별 개별 데이터 Map 복사 후 고유 History ID 주입
                Map<String, String> userData = new HashMap<>(commonData);
                userData.put("id", String.valueOf(historyId));

                Message singleMessage = Message.builder()
                    .setToken(target.getFcmToken())
                    .setNotification(notification)
                    .setAndroidConfig(androidConfig)
                    .putAllData(userData)
                    .build();

                messages.add(singleMessage);
            }

            try {
                BatchResponse response = firebaseMessaging.sendEach(messages);
                log.info("[FCM] 배치 발송 완료. 성공: {}건, 실패: {}건", response.getSuccessCount(), response.getFailureCount());

            } catch (FirebaseMessagingException e) {
                log.error("[FCM Error] 원인: {}, 메시지: {}", e.getMessagingErrorCode(), e.getMessage());
            }
        }
    }
}