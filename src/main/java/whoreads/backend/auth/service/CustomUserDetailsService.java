package whoreads.backend.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import whoreads.backend.auth.principal.CustomUserDetails;
import whoreads.backend.domain.member.repository.MemberRepository;

// 사용자 정보를 불러오고 검증하는 서비스
// 데이터베이스에서 사용자 정보를 가져오고 이를 UserDetails 객체로 반환
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    // 로그인 아이디로 사용자 조회
    @Override
    public UserDetails loadUserByUsername(String loginId) throws UsernameNotFoundException {
        return memberRepository.findByLoginId(loginId)
                .map(CustomUserDetails::new)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + loginId));
    }

    // PK로 사용자 조회
    public UserDetails loadUserById(java.lang.Long id) { // PK로 조회하는 메서드 추가
        return memberRepository.findById(id)
                .map(CustomUserDetails::new)
                .orElseThrow(() -> new UsernameNotFoundException("해당 ID의 사용자를 찾을 수 없습니다: " + id));
    }
}
