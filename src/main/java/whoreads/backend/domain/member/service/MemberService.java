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
}
