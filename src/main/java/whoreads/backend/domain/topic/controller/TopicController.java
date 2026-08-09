package whoreads.backend.domain.topic.controller;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import whoreads.backend.domain.book.dto.BookResponse;
import whoreads.backend.domain.topic.controller.docs.TopicControllerDocs;
import whoreads.backend.domain.topic.dto.TopicResponse;
import whoreads.backend.domain.topic.entity.TopicTag;
import whoreads.backend.domain.topic.service.TopicService;

import java.util.List;

@RestController
@RequestMapping("/api/topics")
@RequiredArgsConstructor
@Validated // previewSize 검증(@Positive) 활성화
public class TopicController implements TopicControllerDocs {

    private final TopicService topicService;

    @Override
    @GetMapping
    public ResponseEntity<List<TopicResponse>> getAllTopics(
            @RequestParam(required = false) TopicTag tag,
            @RequestParam(required = false) @Positive(message = "previewSize는 1 이상이어야 합니다.") Integer previewSize) {
        return ResponseEntity.ok(topicService.getAllTopics(tag, previewSize));
    }

    @Override
    @GetMapping("/{theme}/books")
    public ResponseEntity<List<BookResponse>> getBooksByTopic(
            @PathVariable("theme") TopicTag theme,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        return ResponseEntity.ok(topicService.getBooksByTopic(theme, pageable));
    }
}