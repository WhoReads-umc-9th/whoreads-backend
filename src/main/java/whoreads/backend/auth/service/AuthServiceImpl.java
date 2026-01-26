package whoreads.backend.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import whoreads.backend.auth.dto.AuthReqDto;
import whoreads.backend.auth.dto.AuthResDto;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

    @Override
    public AuthResDto.JoinData signup(AuthReqDto.JoinRequest request) {
        // 더미 데이터 생성
        AuthResDto.MemberInfo dummyMember = new AuthResDto.MemberInfo(
                1L,
                "test",
                "naver.com",
                LocalDateTime.now()
        );

        AuthResDto.JoinData dummyData = new AuthResDto.JoinData(
                "dummy_access_token",
                dummyMember
        );

        return dummyData;
    }

    @Override
    public AuthResDto.LoginData login(AuthReqDto.JoinRequest request) {

        return new AuthResDto.LoginData(
                1L,
                "accessToken",
                "refreshToken"
        );
    }

    @Override
    public void logout(Long memberId) {

        // refreshTokenRepository.deleteByMemberId(memberId);

        // 해당 memberId를 가진 리프레시 토큰이 DB에 있다면 삭제
        log.info("{}번 사용자 로그아웃 - Refresh Token 폐기 완료", memberId);
    }

    @Override
    public AuthResDto.TokenData refresh(AuthReqDto.RefreshRequest request) {

        // 전달받은 리프레시 토큰이 유효한지 확인
        // DB에서 해당 토큰을 가진 사용자가 있는지 확인

        String newAccessToken = "new_access_token";
        String newRefreshToken = "new_refresh_token";

        return new AuthResDto.TokenData(newAccessToken, newRefreshToken);
    }

    @Override
    @Transactional
    public void delete(Long memberId) {
        // 사용자 조회 후 상태를 'INACTIVE'로 변경 (소프트 딜리트)
        // member.setStatus(MemberStatus.INACTIVE);
        // member.setDeletedAt(LocalDateTime.now());

        // 리프레시 토큰은 즉시 삭제하여 접근 차단
        // refreshTokenRepository.deleteByMemberId(memberId);

        log.info("{}번 사용자 회원 탈퇴(Patch) 접수 - 유예 기간 시작", memberId);
    }
}
