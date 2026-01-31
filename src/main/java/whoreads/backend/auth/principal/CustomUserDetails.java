package whoreads.backend.auth.principal;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import whoreads.backend.domain.member.entity.Member;

import java.util.Collection;
import java.util.List;

// UserDetails: 사용자의 아이디, 비밀번호, 권한 등 다양한 정보를 포함한다.
@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {

    private final Member member;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public @Nullable String getPassword() {
        return member.getPassword();
    }

    @Override
    public String getUsername() {
        return member.getLoginId();
    }
}
