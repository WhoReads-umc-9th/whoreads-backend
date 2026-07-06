package whoreads.backend.domain.member.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import whoreads.backend.domain.celebrity.entity.Celebrity;
import whoreads.backend.domain.celebrity.repository.CelebrityRepository;
import whoreads.backend.domain.member.converter.MemberConverter;
import whoreads.backend.domain.member.dto.MemberResDto;
import whoreads.backend.domain.member.entity.Member;
import whoreads.backend.domain.member.entity.MemberCelebrity;
import whoreads.backend.domain.member.repository.MemberCelebrityRepository;
import whoreads.backend.domain.member.repository.MemberRepository;
import whoreads.backend.global.exception.CustomException;
import whoreads.backend.global.exception.ErrorCode;

import whoreads.backend.domain.member.enums.AgeGroup;
import whoreads.backend.domain.member.enums.Gender;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final CelebrityRepository celebrityRepository;
    private final MemberCelebrityRepository memberCelebrityRepository;

    public List<MemberResDto.CelebrityFollow> getFollowList(Long memberId) {
        List<Celebrity> followedCelebrities = memberCelebrityRepository.findCelebritiesByMemberId(memberId);

        return followedCelebrities.stream()
                .map(celebrity -> MemberResDto.CelebrityFollow.builder()
                        .id(celebrity.getId())
                        .name(celebrity.getName())
                        .imageUrl(celebrity.getImageUrl())
                        .shortBio(celebrity.getShortBio())
                        .build())
                .toList();
    }

    public MemberResDto.MemberInfo getMemberInfo(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        return MemberConverter.toMemberInfo(member);
    }

    @Transactional
    public void followCelebrity(Long memberId, Long celebrityId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        Celebrity celebrity = celebrityRepository.findById(celebrityId)
                .orElseThrow(() -> new CustomException(ErrorCode.CELEBRITY_NOT_FOUND));

        // 중복 팔로우 체크
        if (memberCelebrityRepository.existsByMemberAndCelebrity(member, celebrity)) {
            throw new CustomException(ErrorCode.ALREADY_FOLLOWING);
        }

        MemberCelebrity follow = MemberCelebrity.builder()
                .member(member)
                .celebrity(celebrity)
                .build();

        memberCelebrityRepository.save(follow);
    }

    public boolean isFollowingCelebrity(Long memberId, Long celebrityId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        Celebrity celebrity = celebrityRepository.findById(celebrityId)
                .orElseThrow(() -> new CustomException(ErrorCode.CELEBRITY_NOT_FOUND));

        return memberCelebrityRepository.existsByMemberAndCelebrity(member, celebrity);
    }

    @Transactional
    public void unfollowCelebrity(Long memberId, Long celebrityId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        Celebrity celebrity = celebrityRepository.findById(celebrityId)
                .orElseThrow(() -> new CustomException(ErrorCode.CELEBRITY_NOT_FOUND));

        // 팔로우 중인지 먼저 확인
        if (!memberCelebrityRepository.existsByMemberAndCelebrity(member, celebrity)) {
            throw new CustomException(ErrorCode.CELEBRITY_NOT_FOUND); // (에러 코드는 프로젝트에 맞게 수정하세요)
        }

        // 레포지토리에 만들어둔 메서드로 관계 삭제
        memberCelebrityRepository.deleteByMemberAndCelebrity(member, celebrity);
    }

    @Transactional
    public void updateNickname(Long memberId, String nickname) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        member.updateNickname(nickname);
    }

    @Transactional
    public void updateGender(Long memberId, Gender gender) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        member.updateGender(gender);
    }

    @Transactional
    public void updateAgeGroup(Long memberId, AgeGroup ageGroup) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        member.updateAgeGroup(ageGroup);
    }
}
