package whoreads.backend.auth.service;

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
import whoreads.backend.auth.jwt.JwtTokenProvider;
import whoreads.backend.domain.member.converter.MemberConverter;
import whoreads.backend.domain.member.entity.Member;
import whoreads.backend.domain.member.enums.Status;
import whoreads.backend.domain.member.repository.MemberRepository;
import whoreads.backend.global.exception.CustomException;
import whoreads.backend.global.exception.ErrorCode;

import java.time.Duration;
import java.time.LocalDateTime;

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
        Long memberId = Long.parseLong(authentication.getName());

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
}
