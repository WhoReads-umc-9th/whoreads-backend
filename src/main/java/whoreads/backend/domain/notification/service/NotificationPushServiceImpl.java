package whoreads.backend.domain.notification.service;

import com.google.firebase.messaging.*;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import whoreads.backend.domain.member.repository.MemberRepository;
import whoreads.backend.domain.notification.dto.FcmMessageDTO;
import whoreads.backend.global.exception.CustomException;
import whoreads.backend.global.exception.ErrorCode;

import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class NotificationPushServiceImpl implements NotificationPushService {

    private final FirebaseMessaging firebaseMessaging;
    private final MemberRepository memberRepository;

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
                // ios 전용 설정 ( 알림 클릭 시 동작 및 소리 )
                .setApnsConfig(ApnsConfig.builder()
                        .setAps(Aps.builder()
                                .setCategory("CLICK_ACTION")
                                .setSound("default")
                                .build())
                        .build())
                // 커스텀 데이터 ( 프론트 확인용 )
                .putData("title", dto.getTitle())
                .putData("body", dto.getBody())
                .putData("type",dto.getType())
                .putData("link", Optional.ofNullable(dto.getLink()).orElse(""))
                .build();
    }
    
    // 팔로우나 루틴 알림처럼 대량 발송
    @Async("WhoReadsAsyncExecutor")
    @Transactional
    public void sendMulticast(List<String> tokens, FcmMessageDTO dto) {
        if (tokens == null || tokens.isEmpty()) return;

        for (int i = 0; i < tokens.size(); i += 500) {
            List<String> subList = tokens.subList(i, Math.min(i + 500, tokens.size()));

            MulticastMessage message = MulticastMessage.builder()
                    .addAllTokens(subList)
                    .setNotification(Notification.builder()
                            .setTitle(dto.getTitle())
                            .setBody(dto.getBody())
                            .build())
                    .putData("title", dto.getTitle())
                    .putData("body", dto.getBody())
                    .putData("type", dto.getType())
                    .putData("link", Optional.ofNullable(dto.getLink()).orElse(""))
                    .build();
            try {
                BatchResponse response = firebaseMessaging.sendEachForMulticast(message);

                if (response.getFailureCount() > 0) {
                    List<SendResponse> responses = response.getResponses();

                    for (int j = 0; j < responses.size(); j++) {
                        SendResponse res = responses.get(j);

                        // 1. 성공했거나 예외 정보가 없으면 스킵 (Guard Clause)
                        if (res.isSuccessful() || res.getException() == null) {
                            continue;
                        }

                        // 2. 에러 코드 확인 및 토큰 삭제 처리
                        MessagingErrorCode errorCode = res.getException().getMessagingErrorCode();
                        if (errorCode == MessagingErrorCode.UNREGISTERED ||
                                errorCode == MessagingErrorCode.INVALID_ARGUMENT) {

                            memberRepository.clearToken(subList.get(j));
                        }
                    }
                }
            } catch (FirebaseMessagingException e) {
                throw new CustomException(ErrorCode.FCM_SEND_FAILED);
            }
        }
    }
}
