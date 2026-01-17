package whoreads.backend.domain.focusmode.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import whoreads.backend.domain.focusmode.repository.FocusModeRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FocusModeService {

    private final FocusModeRepository focusModeRepository;
}
