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

import java.util.List;


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
        send(createMessage(fcmToken,dto),fcmToken);
    }

    private void send(Message message,String fcmToken) {
        try {
            firebaseMessaging.send(message);
        } catch (FirebaseMessagingException e) {
            MessagingErrorCode errorCode =  e.getMessagingErrorCode();

            //유효하지 않은 토큰인 경우 즉시 삭제
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

            MulticastMessage.Builder builder = MulticastMessage.builder()
                    .addAllTokens(subList.stream()
                            .map(MemberTokenDTO::getFcmToken)
                            .toList())
                    .setNotification(Notification.builder()
                            .setTitle(dto.getTitle())
                            .setBody(dto.getBody())
                            .build())
                    .setAndroidConfig(AndroidConfig.builder()
                            .setPriority(AndroidConfig.Priority.HIGH)
                            .setNotification(AndroidNotification.builder()
                                    .setChannelId("high_importance_channel")
                                    .setIcon("app_logo") // 💡 파일명 매칭
                                    .setClickAction("FLUTTER_NOTIFICATION_CLICK")
                                    .build())
                            .build())
                    .putData("title", dto.getTitle())
                    .putData("body", dto.getBody())
                    .putData("type", dto.getType());
            if (dto.getCelebrityId()!=null && dto.getBookId()!=null)
            {
                builder
                        .putData("celebrity_id", String.valueOf(dto.getCelebrityId()))
                        .putData("book_id", String.valueOf(dto.getBookId()));
            }
            MulticastMessage message = builder.build();
            try {
                BatchResponse response = firebaseMessaging.sendEachForMulticast(message);
                for (int j = 0; j< response.getResponses().size();j++)
                {
                    SendResponse sendResponse = response.getResponses().get(j);
                    MemberTokenDTO tokenDTO = subList.get(j);
                    if (sendResponse.isSuccessful()) {
                        try {
                            notificationHistoryService.saveHistory(tokenDTO.getMemberId(),dto);
                        } catch (Exception e) {
                            log.warn("[History] memberId={} 히스토리 저장 실패",tokenDTO.getMemberId());
                        }
                    }
                }
            } catch (FirebaseMessagingException e) {
                log.error("[FCM Error] 원인: {}, 메시지: {}", e.getMessagingErrorCode(), e.getMessage());
            }
        }

    }
}
