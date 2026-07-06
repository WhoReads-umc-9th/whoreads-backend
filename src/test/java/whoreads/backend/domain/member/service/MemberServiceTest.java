package whoreads.backend.domain.member.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import whoreads.backend.domain.member.entity.Member;
import whoreads.backend.domain.member.enums.AgeGroup;
import whoreads.backend.domain.member.enums.Gender;
import whoreads.backend.domain.member.repository.MemberRepository;
import whoreads.backend.global.exception.CustomException;
import whoreads.backend.global.exception.ErrorCode;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private MemberService memberService;

    private Member member;

    @BeforeEach
    void setUp() {
        member = Member.builder()
                .id(1L)
                .nickname("oldNickname")
                .gender(Gender.MALE)
                .ageGroup(AgeGroup.TEENAGERS)
                .email("test@example.com")
                .loginId("testuser")
                .password("password")
                .build();
    }

    @Test
    @DisplayName("닉네임 변경 성공")
    void updateNickname_success() {
        // given
        given(memberRepository.findById(1L)).willReturn(Optional.of(member));

        // when
        memberService.updateNickname(1L, "newNickname");

        // then
        assertThat(member.getNickname()).isEqualTo("newNickname");
    }

    @Test
    @DisplayName("성별 변경 성공")
    void updateGender_success() {
        // given
        given(memberRepository.findById(1L)).willReturn(Optional.of(member));

        // when
        memberService.updateGender(1L, Gender.FEMALE);

        // then
        assertThat(member.getGender()).isEqualTo(Gender.FEMALE);
    }

    @Test
    @DisplayName("연령대 변경 성공")
    void updateAgeGroup_success() {
        // given
        given(memberRepository.findById(1L)).willReturn(Optional.of(member));

        // when
        memberService.updateAgeGroup(1L, AgeGroup.TWENTIES);

        // then
        assertThat(member.getAgeGroup()).isEqualTo(AgeGroup.TWENTIES);
    }

    @Test
    @DisplayName("회원이 없을 때 예외 발생")
    void updateProfile_memberNotFound() {
        // given
        given(memberRepository.findById(1L)).willReturn(Optional.empty());

        // then
        assertThatThrownBy(() -> memberService.updateNickname(1L, "newNickname"))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.MEMBER_NOT_FOUND.getMessage());
    }
}
