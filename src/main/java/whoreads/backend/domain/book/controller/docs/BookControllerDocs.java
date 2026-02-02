package whoreads.backend.domain.book.controller.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;
import whoreads.backend.domain.book.dto.BookRequest;
import whoreads.backend.domain.book.dto.BookResponse;

import java.util.List;

@Tag(name = "Book (책)", description = "책 조회 및 검색 API")
public interface BookControllerDocs {

    @Operation(summary = "도서 목록 조회 및 검색 (내부 DB)", description = "우리 DB에 저장된 도서 목록을 조회합니다. keyword가 있으면 제목/저자로 검색합니다.")
    List<BookResponse> getAllBooks(@Parameter(description = "검색어 (비워두면 전체 조회)") @RequestParam(required = false) String keyword);

   @Operation(summary = "책 검색", description = "제목 또는 저자로 책을 알라딘으로 검색합니다.")
   List<BookResponse> aladinSearchBooks(@Parameter(description = "검색어") @RequestParam String keyword);

    @Operation(summary = "책 등록 (중복 체크)", description = "책 정보를 등록합니다. 만약 제목과 작가가 동일한 책이 이미 있다면, 새로 저장하지 않고 기존 책 정보를 반환합니다.")
    ResponseEntity<BookResponse> registerBook(@RequestBody BookRequest request);

    @Operation(summary = "가장 많이 추천된 책 (TOP 20)", description = "유명인들이 가장 많이 언급(인용)한 책 20권을 추천 수 내림차순으로 조회합니다.")
    ResponseEntity<List<BookResponse>> getMostRecommendedBooks();
}