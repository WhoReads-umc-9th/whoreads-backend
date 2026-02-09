package whoreads.backend.domain.topic.controller.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import whoreads.backend.domain.topic.dto.TopicResponse;
import java.util.List;

@Tag(name = "Topic (주제 큐레이션)", description = "주제별 추천 도서 조회 API")
public interface TopicControllerDocs {

    @Operation(summary = "주제별 도서 목록 조회", description = "메인 화면에 노출되는 주제별 추천 도서 목록을 조회합니다.")
    ResponseEntity<List<TopicResponse>> getAllTopics();
}