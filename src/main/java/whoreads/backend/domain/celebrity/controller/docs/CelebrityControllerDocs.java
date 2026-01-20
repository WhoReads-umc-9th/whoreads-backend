// package whoreads.backend.domain.celebrity.controller.docs;

// import io.swagger.v3.oas.annotations.Operation;
// import io.swagger.v3.oas.annotations.Parameter;
// import io.swagger.v3.oas.annotations.tags.Tag;
// import org.springframework.web.bind.annotation.PathVariable;
// import whoreads.backend.domain.celebrity.dto.CelebrityResponse; // DTO import

// import java.util.List;

// @Tag(name = "Celebrity (유명인)", description = "유명인 조회 API")
// public interface CelebrityControllerDocs {

//    @Operation(summary = "유명인 전체 조회", description = "모든 유명인 목록을 반환합니다.")
//    List<CelebrityResponse> getAllCelebrities(); // 반환 타입 변경

//    @Operation(summary = "유명인 상세 조회", description = "ID로 특정 유명인 정보를 조회합니다.")
//    CelebrityResponse getCelebrityById(@Parameter(description = "유명인 ID") @PathVariable Long id);
//}