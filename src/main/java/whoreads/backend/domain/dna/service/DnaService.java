package whoreads.backend.domain.dna.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import whoreads.backend.domain.dna.repository.DnaTypeRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DnaService {

    private final DnaTypeRepository dnaTypeRepository;
}
