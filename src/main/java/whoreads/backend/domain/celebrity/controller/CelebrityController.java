package whoreads.backend.domain.celebrity.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import whoreads.backend.domain.celebrity.service.CelebrityService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/celebrities")
public class CelebrityController {

    private final CelebrityService celebrityService;
}
