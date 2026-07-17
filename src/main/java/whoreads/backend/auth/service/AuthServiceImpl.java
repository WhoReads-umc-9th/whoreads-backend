package whoreads.backend.auth.service;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import whoreads.backend.auth.dto.AuthReqDto;
import whoreads.backend.auth.dto.AuthResDto;
import whoreads.backend.auth.dto.KakaoTokenResponseDto;
import whoreads.backend.auth.dto.KakaoUserInfoDto;
import whoreads.backend.auth.jwt.JwtTokenProvider;
import whoreads.backend.auth.principal.CustomUserDetails;
import whoreads.backend.domain.member.converter.MemberConverter;
import whoreads.backend.domain.member.entity.Member;
import whoreads.backend.domain.member.enums.Provider;
import whoreads.backend.domain.member.enums.Status;
import whoreads.backend.domain.member.repository.MemberRepository;
import whoreads.backend.global.exception.CustomException;
import whoreads.backend.global.exception.ErrorCode;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final JwtTokenProvider jwtTokenProvider;
    private final EmailService emailService;
    private final StringRedisTemplate redisTemplate;
    private final KakaoOAuthClient kakaoOAuthClient;

    @Override
    public AuthResDto.JoinData signup(AuthReqDto.SignUpRequest dto) {

        String email = dto.request().email();
        // 이메일 인증 체크 (테스트 시 주석)
        if (!emailService.isVerified(email))
            throw new CustomException(ErrorCode.UNAUTHORIZED);

        // 중복 체크
        validateDuplicateEmail(email);
        checkLoginIdDuplicate(dto.request().loginId());

        String encodedPassword = passwordEncoder.encode(dto.request().password());

        // 데이터베이스에 사용자 저장
        Member member = MemberConverter.toMember(dto, encodedPassword);
        memberRepository.save(member);

        redisTemplate.delete("VERIFIED_" + email);

        String token = jwtTokenProvider.createAccessToken(member.getId());

        return MemberConverter.toJoinData(member, token);
    }

    @Override
    public void checkLoginIdDuplicate(String loginId) {
        if (memberRepository.existsByLoginId(loginId))
            throw new CustomException(ErrorCode.DUPLICATE_ID);
    }

    public void validateDuplicateEmail(String email) {
        if (memberRepository.existsByEmail(email))
            throw new CustomException(ErrorCode.DUPLICATE_EMAIL);
    }

    @Override
    public AuthResDto.TokenData login(AuthReqDto.LoginRequest request) {
        // 1. 인증 시도
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(request.loginId(), request.password());

        // 2. DB의 사용자와 대조 (여기서 CustomUserDetailsService가 실행됨)
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);

        // 3. 인증 결과에서 ID 추출 (Long 타입으로 변환)
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long memberId = userDetails.getMember().getId();

        AuthResDto.TokenData tokenData = jwtTokenProvider.generateTokenResponse(memberId);

        redisTemplate.opsForValue().set(
                "RT:" + memberId,
                tokenData.refreshToken(),
                Duration.ofDays(7)
        );

        // 4. 요구사항에 맞춰 토큰 생성 후 반환
        return tokenData;
    }

    @Override
    public void logout(Long memberId) {
        redisTemplate.delete("RT:" + memberId);
        log.info("{}번 사용자 로그아웃 - Refresh Token 삭제", memberId);
    }

    @Override
    public AuthResDto.TokenData refresh(AuthReqDto.RefreshRequest request) {

        String refreshToken = request.refreshToken();

        // 전달받은 리프레시 토큰이 유효한지 확인
        if (!jwtTokenProvider.validateToken(refreshToken))
            throw new RuntimeException("유효하지 않은 리프레시 토큰입니다.");

        Long memberId = jwtTokenProvider.getMemberIdFromToken(refreshToken);

        // 토큰 재발급
        return jwtTokenProvider.generateTokenResponse(memberId);
    }

    @Override
    public AuthResDto.KakaoLoginData kakaoLogin(AuthReqDto.KakaoLoginRequest request) {
        // 1. 인가 코드로 카카오 액세스 토큰 교환 후, 사용자 정보 조회
        KakaoTokenResponseDto kakaoToken = kakaoOAuthClient.requestToken(request.code());
        KakaoUserInfoDto userInfo = kakaoOAuthClient.requestUserInfo(kakaoToken.accessToken());

        String providerId = String.valueOf(userInfo.id());

        // 2. 이미 가입된 카카오 회원이면 바로 로그인 처리
        Member member = memberRepository.findByProviderAndProviderId(Provider.KAKAO, providerId).orElse(null);
        if (member != null) {
            AuthResDto.TokenData tokenData = jwtTokenProvider.generateTokenResponse(member.getId());
            redisTemplate.opsForValue().set(
                    "RT:" + member.getId(),
                    tokenData.refreshToken(),
                    Duration.ofDays(7)
            );

            return AuthResDto.KakaoLoginData.builder()
                    .isNewMember(false)
                    .tokenData(tokenData)
                    .build();
        }

        // 3. 신규 회원이면 닉네임/성별/연령대를 입력받기 위해 임시 가입용 토큰만 발급
        String email = userInfo.kakaoAccount() != null ? userInfo.kakaoAccount().email() : null;
        if (email == null)
            throw new CustomException(ErrorCode.KAKAO_EMAIL_REQUIRED);

        String nickname = (userInfo.kakaoAccount() != null && userInfo.kakaoAccount().profile() != null)
                ? userInfo.kakaoAccount().profile().nickname()
                : null;

        String registrationToken = jwtTokenProvider.createKakaoRegistrationToken(providerId, email, nickname);

        return AuthResDto.KakaoLoginData.builder()
                .isNewMember(true)
                .registrationToken(registrationToken)
                .email(email)
                .nickname(nickname)
                .build();
    }

    @Override
    public AuthResDto.TokenData kakaoSignup(AuthReqDto.KakaoSignUpRequest request) {
        Claims claims = jwtTokenProvider.parseKakaoRegistrationToken(request.registrationToken());
        String providerId = claims.get("providerId", String.class);
        String email = claims.get("email", String.class);

        if (memberRepository.existsByProviderAndProviderId(Provider.KAKAO, providerId))
            throw new CustomException(ErrorCode.ALREADY_REGISTERED_KAKAO);
        validateDuplicateEmail(email);

        Member member = Member.builder()
                .provider(Provider.KAKAO)
                .providerId(providerId)
                .loginId("kakao_" + providerId)
                .email(email)
                .nickname(request.nickname())
                .gender(request.gender())
                .ageGroup(request.ageGroup())
                // 소셜 로그인 회원은 비밀번호 로그인을 쓰지 않으므로, 알 수 없는 랜덤 값으로 채워 NOT NULL 제약만 만족시킨다.
                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                .build();

        memberRepository.save(member);

        AuthResDto.TokenData tokenData = jwtTokenProvider.generateTokenResponse(member.getId());
        redisTemplate.opsForValue().set(
                "RT:" + member.getId(),
                tokenData.refreshToken(),
                Duration.ofDays(7)
        );

        return tokenData;
    }

    @Override
    @Transactional
    public void delete(Long memberId) {
        if (memberId == null)
            throw new CustomException(ErrorCode.UNAUTHORIZED);

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        member.setStatus(Status.INACTIVE);
        member.setDeletedAt(LocalDateTime.now());

        // 리프레시 토큰은 즉시 삭제하여 접근 차단
        redisTemplate.delete("RT:" + memberId);

        log.info("{}번 사용자 회원 탈퇴(Patch) 접수 - 유예 기간 시작", memberId);
    }

    @Override
    public void changePassword(Long memberId, AuthReqDto.PasswordChangeRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        // 현재 비밀번호가 데이터베이스에 저장된 비밀번호와 맞는지 확인
        if (!passwordEncoder.matches(request.currentPassword(), member.getPassword()))
            throw new CustomException(ErrorCode.PASSWORD_MISMATCH);

        // 새 비밀번호와 확인용 비밀번호 일치하는지 확인
        if (!request.newPassword().equals(request.confirmPassword()))
            throw new CustomException(ErrorCode.PASSWORD_MISMATCH);

        // 새 비밀번호 암호화 및 업데이트
        String newPassword = passwordEncoder.encode(request.newPassword());
        member.updatePassword(newPassword);
    }
}
