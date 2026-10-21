package whoreads.backend.domain.celebrity.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import whoreads.backend.domain.celebrity.dto.CelebrityCategoryResponse;
import whoreads.backend.domain.celebrity.dto.CelebrityDetailResponse;
import whoreads.backend.domain.celebrity.dto.CelebrityResponse;
import whoreads.backend.domain.celebrity.entity.Celebrity;
import whoreads.backend.domain.celebrity.entity.CelebrityTag;
import whoreads.backend.domain.celebrity.repository.CelebrityRepository;
import whoreads.backend.global.exception.CustomException;
import whoreads.backend.global.exception.ErrorCode;
import whoreads.backend.infra.s3.S3Service;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CelebrityService {

    private final CelebrityRepository celebrityRepository;
    private final S3Service s3Service;

    // 인물 카테고리(직업 태그) 목록 조회 - 프론트 하드코딩 제거용
    public List<CelebrityCategoryResponse> getCategories() {
        // 화면에서 description(한글 명칭)을 쓰므로 가나다순으로 정렬해서 내려준다
        return Arrays.stream(CelebrityTag.values())
                .sorted(Comparator.comparing(CelebrityTag::getDescription))
                .map(CelebrityCategoryResponse::from)
                .collect(Collectors.toList());
    }

    // 전체 조회 혹은 태그 필터링 조회
    public List<CelebrityResponse> getCelebrities(CelebrityTag tag) {
        List<Celebrity> celebrities;

        if (tag == null) {
            celebrities = celebrityRepository.findAll();
        } else {
            celebrities = celebrityRepository.findAllByJobTagsContains(tag);
        }

        return celebrities.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // 상세 조회 (ID)
    public CelebrityDetailResponse getCelebrity(Long id) {
        // 바꾼 이유: 기본 EntityNotFoundException 대신 프로젝트 공통 CustomException과 이미 정의된 CELEBRITY_NOT_FOUND 사용
        Celebrity celebrity = celebrityRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.CELEBRITY_NOT_FOUND));

        return CelebrityDetailResponse.from(celebrity, s3Service.generateUrl(celebrity.getImageUrl()));
    }

    private CelebrityResponse toResponse(Celebrity celebrity) {
        return CelebrityResponse.from(celebrity, s3Service.generateUrl(celebrity.getImageUrl()));
    }
}