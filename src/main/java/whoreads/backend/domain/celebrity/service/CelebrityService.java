package whoreads.backend.domain.celebrity.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import whoreads.backend.domain.celebrity.dto.CelebrityResponse;
import whoreads.backend.domain.celebrity.entity.Celebrity;
import whoreads.backend.domain.celebrity.entity.CelebrityTag;
import whoreads.backend.domain.celebrity.repository.CelebrityRepository;
import whoreads.backend.global.exception.GlobalExceptionHandler;

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
            // 태그가 없으면(전체) -> 모두 조회
            celebrities = celebrityRepository.findAll();
        } else {
            // 태그가 있으면(직업별) -> 필터링 조회
            celebrities = celebrityRepository.findAllByJobTagsContains(tag);
        }

        return celebrities.stream()
                .map(CelebrityResponse::from)
                .collect(Collectors.toList());
    }

    // 상세 조회 (ID)
    public CelebrityResponse getCelebrity(Long id) {
        Celebrity celebrity = celebrityRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유명인입니다. ID=" + id));

        return CelebrityResponse.from(celebrity);
    }
}