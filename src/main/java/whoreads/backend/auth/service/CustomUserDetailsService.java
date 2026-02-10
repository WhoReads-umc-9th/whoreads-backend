package whoreads.backend.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import whoreads.backend.domain.member.repository.MemberRepository;

// 사용자 정보를 불러오고 검증하는 서비스
// 데이터베이스에서 사용자 정보를 가져오고 이를 UserDetails 객체로 반환
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String loginId) throws UsernameNotFoundException {
        return memberRepository.findByLoginId(loginId)
                .map(member -> User.builder()
                        .username(String.valueOf(member.getId()))
                        .password(member.getPassword())
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + loginId));
    }
}
