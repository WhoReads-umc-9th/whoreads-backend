package whoreads.backend.domain.topic.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import whoreads.backend.domain.topic.controller.docs.TopicControllerDocs;
import whoreads.backend.domain.topic.dto.TopicResponse;
import whoreads.backend.domain.topic.service.TopicService;

import java.util.List;

@RestController
@RequestMapping("/api/topics")
@RequiredArgsConstructor
public class TopicController implements TopicControllerDocs {

    private final TopicService topicService;

    @Override
    @GetMapping
    public ResponseEntity<List<TopicResponse>> getAllTopics() {
        return ResponseEntity.ok(topicService.getAllTopics());
    }
}