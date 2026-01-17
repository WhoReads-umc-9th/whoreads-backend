package whoreads.backend.domain.focusmode.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import whoreads.backend.domain.focusmode.service.FocusModeService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/focus-mode")
public class FocusModeController {

    private final FocusModeService focusModeService;
}
