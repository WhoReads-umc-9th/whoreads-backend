package whoreads.backend.domain.notification.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import whoreads.backend.domain.member.repository.MemberRepository;
import whoreads.backend.domain.notification.converter.NotificationConverter;
import whoreads.backend.domain.notification.dto.FcmMessageDTO;
import whoreads.backend.domain.notification.dto.NotificationResDTO;
import whoreads.backend.domain.notification.entity.NotificationHistory;
import whoreads.backend.domain.notification.enums.NotificationType;
import whoreads.backend.domain.notification.repository.NotificationHistoryRepository;
import whoreads.backend.global.exception.CustomException;
import whoreads.backend.global.exception.ErrorCode;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationHistoryServiceImpl implements NotificationHistoryService {
    private final NotificationHistoryRepository notificationHistoryRepository;
    private final MemberRepository memberRepository;

    @Transactional
    @Override
    public NotificationResDTO.TotalInboxDTO getNotificationHistory(Long memberId, Long cursor, int size) {
        Pageable pageable = PageRequest.of(0, size + 1);
        // DB에서 size + 1 만큼 조회
        List<NotificationHistory> results =
                notificationHistoryRepository
                        .findNotificationHistoriesWithCursor(memberId,cursor,pageable);
        return NotificationConverter.toTotalInboxDTO(results,size);
    }

    @Override
    @Transactional
    public void readNotification(Long memberId, Long notificationId) {
        // 이 알림의 권한이 사용자인지 확인
        NotificationHistory history = notificationHistoryRepository.findById(notificationId).orElseThrow(()->
                new CustomException(ErrorCode.RESOURCE_NOT_FOUND,"알림이 존재하지 않습니다."));
        if (!Objects.equals(history.getMember().getId(), memberId))
            throw new CustomException(ErrorCode.ACCESS_DENIED,"사용자의 알림이 아닙니다.");
        notificationHistoryRepository.deleteById(notificationId);
    }
    @Transactional
    public void saveHistory(Long memberId, FcmMessageDTO dto) {
        NotificationHistory history = NotificationHistory.builder()
                .member(memberRepository.getReferenceById(memberId))
                .title(dto.getTitle())
                .body(dto.getBody())
                .type(NotificationType.valueOf(dto.getType()))
                .link(dto.getLink())
                .build();
        notificationHistoryRepository.save(history);
    }
}
