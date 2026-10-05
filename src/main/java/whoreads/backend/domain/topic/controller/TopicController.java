package whoreads.backend.domain.topic.controller;

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
// previewSize 검증(@Positive) 활성화.
// 제약은 TopicControllerDocs 쪽에만 선언한다 - 구현체에 중복 선언하면 Bean Validation 규칙(HV000151,
// "오버라이드 메서드는 상위 메서드의 파라미터 제약을 재정의할 수 없다")에 걸려 이 컨트롤러의
// validation 메타데이터 빌드가 실패하고, 결과적으로 /api/topics 계열 전체가 500이 된다.
@Validated
public class TopicController implements TopicControllerDocs {

    private final TopicService topicService;

    @Override
    @GetMapping
    public ResponseEntity<List<TopicResponse>> getAllTopics(
            @RequestParam(required = false) TopicTag tag,
            @RequestParam(required = false) Integer previewSize) {
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