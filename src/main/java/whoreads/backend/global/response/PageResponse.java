package whoreads.backend.global.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 목록 조회 API 공용 페이징 응답.
 * 만든 이유: 도서 목록처럼 전체를 통째로 내려주던 API에 페이징을 붙이면서,
 * 프론트가 다음 페이지 존재 여부/총 개수를 알 수 있도록 메타 정보를 함께 내려주기 위함.
 */
@Getter
@Builder
public class PageResponse<T> {

    private List<T> content;

    private int page;          // 현재 페이지 번호 (0부터 시작)
    private int size;          // 페이지당 개수

    @JsonProperty("total_elements") private long totalElements;
    @JsonProperty("total_pages") private int totalPages;

    @JsonProperty("has_next") private boolean hasNext;
    @JsonProperty("is_first") private boolean first;
    @JsonProperty("is_last") private boolean last;

    public static <T> PageResponse<T> from(Page<T> page) {
        return build(page, page.getContent());
    }

    /** 엔티티 Page를 응답 DTO로 변환하면서 감싸는 용도 */
    public static <S, T> PageResponse<T> of(Page<S> page, Function<S, T> mapper) {
        return build(page, page.getContent().stream()
                .map(mapper)
                .collect(Collectors.toList()));
    }

    private static <S, T> PageResponse<T> build(Page<S> page, List<T> content) {
        return PageResponse.<T>builder()
                .content(content)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .hasNext(page.hasNext())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }
}
