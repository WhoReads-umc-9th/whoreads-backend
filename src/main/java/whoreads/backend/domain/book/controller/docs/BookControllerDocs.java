// package whoreads.backend.domain.book.controller.docs;

// import io.swagger.v3.oas.annotations.Operation;
// import io.swagger.v3.oas.annotations.Parameter;
// import io.swagger.v3.oas.annotations.tags.Tag;
// import org.springframework.web.bind.annotation.RequestParam;
// import whoreads.backend.domain.book.dto.BookResponse;

// import java.util.List;

// @Tag(name = "Book (책)", description = "책 조회 및 검색 API")
// public interface BookControllerDocs {

//    @Operation(summary = "책 전체 조회", description = "모든 책 목록을 반환합니다.")
//    List<BookResponse> getAllBooks();

//    @Operation(summary = "책 검색", description = "제목 또는 저자로 책을 검색합니다.")
//    List<BookResponse> searchBooks(@Parameter(description = "검색어") @RequestParam String keyword);
//}