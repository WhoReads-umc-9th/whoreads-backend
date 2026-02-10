package whoreads.backend.domain.celebrity.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import whoreads.backend.domain.celebrity.dto.CelebrityResponse;
import whoreads.backend.domain.celebrity.entity.Celebrity;
import whoreads.backend.domain.celebrity.entity.CelebrityTag;
import whoreads.backend.domain.celebrity.repository.CelebrityRepository;

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
        Celebrity celebrity = celebrityRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 유명인입니다. ID=" + id));

        return CelebrityResponse.from(celebrity);
    }
}