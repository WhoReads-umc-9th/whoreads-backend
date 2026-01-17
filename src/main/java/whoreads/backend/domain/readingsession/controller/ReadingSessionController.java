package whoreads.backend.domain.readingsession.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import whoreads.backend.domain.readingsession.service.ReadingSessionService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reading-sessions")
public class ReadingSessionController {

    private final ReadingSessionService readingSessionService;
}
