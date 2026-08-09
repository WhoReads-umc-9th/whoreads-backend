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

    @Operation(summary = "주제별 도서 목록 조회", description = "메인 화면에 노출되는 주제별 추천 도서 목록을 조회합니다. "
            + "tag를 넘기면 해당 카테고리만 필터링하고, 비우면 전체를 조회합니다. "
            + "previewSize를 넘기면 주제별로 그 개수만큼만(미리보기), 비우면 주제별 전체 도서를 반환합니다.")
    ResponseEntity<List<TopicResponse>> getAllTopics(
            @Parameter(description = "주제 카테고리 태그 (예: SOCIETY, MINDSET). 비워두면 전체 조회")
            TopicTag tag,
            @Parameter(description = "주제별 미리보기 도서 개수 (예: 3). 비워두면 주제별 전체 반환. 실제 도서가 더 적으면 있는 만큼만 반환")
            Integer previewSize
    );

    @Operation(summary = "특정 주제별 도서 목록 조회", description = "선택한 특정 주제 탭에 해당하는 도서 목록을 페이징하여 조회합니다.")
    ResponseEntity<List<BookResponse>> getBooksByTopic(
            @Parameter(description = "주제 태그 (예: LIFE_DIRECTION, MINDSET 등)", required = true) TopicTag theme,
            @ParameterObject Pageable pageable
    );
}