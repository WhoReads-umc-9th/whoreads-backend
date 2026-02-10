package whoreads.backend.domain.member.converter;

import whoreads.backend.auth.dto.AuthReqDto;
import whoreads.backend.auth.dto.AuthResDto;
import whoreads.backend.domain.member.entity.Member;

public class MemberConverter {

    public static Member toMember(AuthReqDto.SignUpRequest request, String password) {
        return Member.builder()
                .loginId(request.request().loginId())
                .email(request.request().email())
                .nickname(request.memberInfo().nickname())
                .gender(request.memberInfo().gender())
                .ageGroup(request.memberInfo().ageGroup())
                .password(password)
                .build();
    }

    public static AuthResDto.JoinData toJoinData(Member member, String accessToken) {

        // 하위 정보인 MemberInfo 생성
        AuthResDto.MemberInfo memberInfo = AuthResDto.MemberInfo.builder()
                .id(member.getId())
                .loginId(member.getLoginId())
                .email(member.getEmail())
                .createdAt(member.getCreatedAt())
                .build();

        // 최종 JoinData
        return AuthResDto.JoinData.builder()
                .accessToken(accessToken)
                .member(memberInfo)
                .build();
    }
}
