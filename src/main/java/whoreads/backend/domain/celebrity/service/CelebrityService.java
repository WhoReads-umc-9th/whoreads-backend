package whoreads.backend.domain.celebrity.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import whoreads.backend.domain.celebrity.dto.CelebrityCategoryResponse;
import whoreads.backend.domain.celebrity.dto.CelebrityImageRequest;
import whoreads.backend.domain.celebrity.dto.CelebrityImageResponse;
import whoreads.backend.domain.celebrity.dto.CelebrityResponse;
import whoreads.backend.domain.celebrity.entity.Celebrity;
import whoreads.backend.domain.celebrity.entity.CelebrityTag;
import whoreads.backend.domain.celebrity.repository.CelebrityRepository;
import whoreads.backend.global.exception.CustomException;
import whoreads.backend.global.exception.ErrorCode;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CelebrityService {

    private final CelebrityRepository celebrityRepository;

    // 인물 카테고리(직업 태그) 목록 조회 - 프론트 하드코딩 제거용
    public List<CelebrityCategoryResponse> getCategories() {
        return Arrays.stream(CelebrityTag.values())
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
                .map(CelebrityResponse::from)
                .collect(Collectors.toList());
    }

    // 상세 조회 (ID)
    public CelebrityResponse getCelebrity(Long id) {
        // 바꾼 이유: 기본 EntityNotFoundException 대신 프로젝트 공통 CustomException과 이미 정의된 CELEBRITY_NOT_FOUND 사용
        Celebrity celebrity = celebrityRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.CELEBRITY_NOT_FOUND));

        return CelebrityResponse.from(celebrity);
    }

    // 유명인 프로필 이미지 조회
    public CelebrityImageResponse getCelebrityImage(Long id) {
        Celebrity celebrity = celebrityRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.CELEBRITY_NOT_FOUND));

        return CelebrityImageResponse.from(celebrity.getId(), celebrity.getImageUrl());
    }

    // 유명인 프로필 이미지 수정 (PATCH)
    @Transactional
    public CelebrityImageResponse updateCelebrityImage(Long id, CelebrityImageRequest request) {
        Celebrity celebrity = celebrityRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.CELEBRITY_NOT_FOUND));

        // 변경 감지(Dirty Checking)로 업데이트 처리
        celebrity.updateImageUrl(request.getImageUrl());

        return CelebrityImageResponse.from(celebrity.getId(), celebrity.getImageUrl());
    }
}