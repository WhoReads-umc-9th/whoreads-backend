package whoreads.backend.domain.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import whoreads.backend.domain.member.entity.Member;
import whoreads.backend.domain.member.repository.MemberRepository;
import whoreads.backend.global.exception.CustomException;
import whoreads.backend.global.exception.ErrorCode;

import java.time.LocalDateTime;


@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationTokenServiceImpl implements NotificationTokenService {
    private final MemberRepository memberRepository;

    @Override
    @Transactional
    public void updateToken(Long userId, String token){
        memberRepository.clearToken(token);
        Member member = memberRepository.findById(userId)
                .orElseThrow(()->new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        // 이 토큰을 이미 가지고 있는 다른 유저들의 토큰을 먼저 삭제
        // 내 토큰 업데이트
        member.updateFcmToken(token);
    }
    @Override
    @Transactional
    public void deleteToken(Long userId){
        Member member = memberRepository.findById(userId)
                .orElseThrow(()->new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        member.updateFcmToken(null);
    }
    @Transactional(readOnly = true)
    @Override
    public String getToken(Long userId){
        return memberRepository.findById(userId)
                .map(Member::getFcmToken)
                .orElse(null);
    }
    /*
    * 매일 2시 30일 지난 토큰 정리
    * */
    @Scheduled(cron = "0 0 2 * * *",zone = "Asia/Seoul")
    @Transactional
    public void deleteInactiveUsers() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(30);
        memberRepository.clearExpiredTokens(threshold);
        log.info("미접속자 토큰 제거 완료");
    }
}
