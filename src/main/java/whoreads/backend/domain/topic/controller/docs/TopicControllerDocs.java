package whoreads.backend.domain.topic.controller.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import whoreads.backend.domain.book.dto.BookResponse;
import whoreads.backend.domain.topic.dto.TopicResponse;
import whoreads.backend.domain.topic.entity.TopicTag;

import java.util.List;

@Tag(name = "Topic (주제 큐레이션)", description = "주제별 추천 도서 조회 API")
public interface TopicControllerDocs {

    @Operation(summary = "주제별 도서 목록 조회", description = "메인 화면에 노출되는 주제별 추천 도서 목록을 조회합니다.") // 바꾼 이유: API의 목적을 명확히 함
    ResponseEntity<List<TopicResponse>> getAllTopics();

    @Operation(summary = "특정 주제별 도서 목록 조회", description = "선택한 특정 주제 탭에 해당하는 도서 목록을 페이징하여 조회합니다.")
    ResponseEntity<List<BookResponse>> getBooksByTopic(
            @Parameter(description = "주제 태그 (예: LIFE_DIRECTION, MINDSET 등)", required = true) TopicTag theme,
            @ParameterObject Pageable pageable
    );
}