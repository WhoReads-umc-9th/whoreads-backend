package whoreads.backend.domain.celebrity.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import whoreads.backend.domain.celebrity.repository.CelebrityRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CelebrityService {

    private final CelebrityRepository celebrityRepository;
}
