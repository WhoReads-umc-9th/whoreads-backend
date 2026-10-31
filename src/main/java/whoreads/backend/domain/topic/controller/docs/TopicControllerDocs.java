package whoreads.backend.domain.topic.controller.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
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
            + "previewSize를 넘기면 주제별로 그 개수만큼만(미리보기), 비우면 주제별 전체 도서를 반환합니다.\n\n"
            + "응답의 bookCount는 previewSize와 무관하게 해당 주제의 전체 도서 권수입니다. "
            + "메인 배너처럼 미리보기 N권 + 총 권수가 함께 필요하면 `?tag={theme}&previewSize=N` 한 번으로 받을 수 있습니다.")
    ResponseEntity<List<TopicResponse>> getAllTopics(
            @Parameter(description = "주제 카테고리 태그 (예: SOCIETY, MINDSET). 대소문자/하이픈 표기와 한글 이름(\"사고방식\")도 허용합니다. 비워두면 전체 조회")
            TopicTag tag,
            // 제약은 인터페이스에만 선언한다 (구현체에 중복 선언하면 HV000151로 컨트롤러 전체가 500)
            @Parameter(description = "주제별 미리보기 도서 개수 (예: 3). 비워두면 주제별 전체 반환. 실제 도서가 더 적으면 있는 만큼만 반환")
            @Positive(message = "previewSize는 1 이상이어야 합니다.")
            Integer previewSize
    );

    @Operation(summary = "특정 주제별 도서 목록 조회 (주제별 조회 표준 엔드포인트)",
            description = "선택한 특정 주제 탭에 해당하는 도서 목록을 페이징하여 조회합니다.\n\n"
                    + "TOP_20을 넘기면 인용(추천) 수 기준 상위 도서를 반환합니다.\n\n"
                    + "같은 기능의 `GET /api/books/themes/{theme}`, `GET /api/books/most-recommended`는 "
                    + "하위호환용으로만 남겨두었으니 신규 연동은 이 엔드포인트를 사용해주세요.")
    ResponseEntity<List<BookResponse>> getBooksByTopic(
            @Parameter(description = "주제 태그 (예: LIFE_DIRECTION, MINDSET). /api/topics 응답의 tag 값을 그대로 넣으면 됩니다. 대소문자/하이픈 표기와 한글 이름도 허용하며, 알 수 없는 값은 400입니다.", required = true) TopicTag theme,
            @ParameterObject Pageable pageable
    );
}