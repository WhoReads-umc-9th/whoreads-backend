package whoreads.backend.domain.dna.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import whoreads.backend.domain.dna.service.DnaService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/dna")
public class DnaController {

    private final DnaService dnaService;
}
