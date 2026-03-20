package whoreads.backend.domain.celebrity.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import whoreads.backend.domain.celebrity.dto.CelebrityResponse;
import whoreads.backend.domain.celebrity.entity.Celebrity;
import whoreads.backend.domain.celebrity.entity.CelebrityTag;
import whoreads.backend.domain.celebrity.repository.CelebrityRepository;
import whoreads.backend.global.exception.CustomException;
import whoreads.backend.global.exception.ErrorCode;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CelebrityService {

    private final CelebrityRepository celebrityRepository;

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
}