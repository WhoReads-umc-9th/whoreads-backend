package whoreads.backend.domain.topic.controller;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.executable.ExecutableValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import whoreads.backend.domain.topic.dto.TopicResponse;
import whoreads.backend.domain.topic.entity.Topic;
import whoreads.backend.domain.topic.entity.TopicTag;
import whoreads.backend.domain.topic.service.TopicService;
import whoreads.backend.global.converter.StringToTopicTagConverter;
import whoreads.backend.global.exception.GlobalExceptionHandler;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 이슈 #224: /api/topics 계열 전체가 500으로 내려온 문제에 대한 회귀 테스트.
 * <p>
 * 원인은 세 가지였다.
 * (1) previewSize의 @Positive 제약이 구현체와 인터페이스에 엇갈려 선언돼 HV000151로
 *     컨트롤러의 validation 메타데이터 빌드가 실패 - 모든 엔드포인트가 500 (근본 원인)
 * (2) 응답에 enum 상수명이 없어 프론트가 {theme}에 넣을 값을 알 수 없었던 점
 * (3) enum 변환 실패가 400이 아니라 Exception catch-all을 타고 500으로 나간 점
 */
class TopicControllerTest {

    private final TopicService topicService = Mockito.mock(TopicService.class);

    private MockMvc mockMvc() {
        DefaultFormattingConversionService conversionService = new DefaultFormattingConversionService();
        conversionService.addConverter(new StringToTopicTagConverter());

        return MockMvcBuilders.standaloneSetup(new TopicController(topicService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setConversionService(conversionService)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Test
    @DisplayName("주제 목록 응답에 프론트가 조회에 쓸 enum 상수명(tag)과 주제 전체 도서 권수(bookCount)가 포함된다")
    void getAllTopics_includesEnumTagAndBookCount() throws Exception {
        // 미리보기로 books는 비어 있어도 bookCount는 주제 전체 권수를 그대로 내려줘야 한다 (배너 "총 N권" 하드코딩 제거용)
        Mockito.when(topicService.getAllTopics(Mockito.any(), Mockito.any()))
                .thenReturn(List.of(TopicResponse.of(Topic.builder().name(TopicTag.MINDSET).build(), List.of(), 17)));

        mockMvc().perform(get("/api/topics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tag").value("MINDSET"))
                .andExpect(jsonPath("$[0].name").value("사고방식"))
                .andExpect(jsonPath("$[0].bookCount").value(17))
                .andExpect(jsonPath("$[0].books").isEmpty());
    }

    @Test
    @DisplayName("주제값은 상수명/소문자/하이픈/한글 이름 모두 같은 TopicTag로 변환된다")
    void getBooksByTopic_acceptsAlternateNotations() throws Exception {
        Mockito.when(topicService.getBooksByTopic(Mockito.any(), Mockito.any())).thenReturn(List.of());

        for (String theme : List.of("MINDSET", "mindset", "사고방식")) {
            mockMvc().perform(get("/api/topics/{theme}/books", theme))
                    .andExpect(status().isOk());
        }
        mockMvc().perform(get("/api/topics/{theme}/books", "top-20"))
                .andExpect(status().isOk());

        ArgumentCaptor<TopicTag> captor = ArgumentCaptor.forClass(TopicTag.class);
        Mockito.verify(topicService, Mockito.times(4)).getBooksByTopic(captor.capture(), Mockito.any());
        assertThat(captor.getAllValues())
                .containsExactly(TopicTag.MINDSET, TopicTag.MINDSET, TopicTag.MINDSET, TopicTag.TOP_20);
    }

    @Test
    @DisplayName("tag 쿼리파라미터도 한글 이름을 허용하고, 빈 값은 필터 없음으로 처리한다")
    void getAllTopics_acceptsKoreanTagAndBlank() throws Exception {
        Mockito.when(topicService.getAllTopics(Mockito.any(), Mockito.any())).thenReturn(List.of());

        // TopicTag 전용 변환기를 끼워도 다른 파라미터 변환(previewSize)이 깨지지 않는지 함께 확인
        mockMvc().perform(get("/api/topics").param("previewSize", "3")).andExpect(status().isOk());
        mockMvc().perform(get("/api/topics").param("tag", "사고방식")).andExpect(status().isOk());
        mockMvc().perform(get("/api/topics").param("tag", "")).andExpect(status().isOk());

        ArgumentCaptor<TopicTag> tagCaptor = ArgumentCaptor.forClass(TopicTag.class);
        ArgumentCaptor<Integer> previewSizeCaptor = ArgumentCaptor.forClass(Integer.class);
        Mockito.verify(topicService, Mockito.times(3))
                .getAllTopics(tagCaptor.capture(), previewSizeCaptor.capture());
        assertThat(tagCaptor.getAllValues()).containsExactly(null, TopicTag.MINDSET, null);
        assertThat(previewSizeCaptor.getAllValues()).containsExactly(3, null, null);
    }

    @Test
    @DisplayName("알 수 없는 주제값은 500이 아니라 허용값을 안내하는 400으로 응답한다")
    void getBooksByTopic_unknownTheme_returnsBadRequest() throws Exception {
        for (String theme : List.of("NOT_A_TOPIC", "1")) {
            mockMvc().perform(get("/api/topics/{theme}/books", theme))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.is_success").value(false))
                    .andExpect(jsonPath("$.code").value(400))
                    .andExpect(jsonPath("$.message").value(
                            org.hamcrest.Matchers.containsString("LIFE_DIRECTION, HUMAN_UNDERSTANDING, SOCIETY, MINDSET, TURNING_POINT, TOP_20")));
        }

        Mockito.verify(topicService, Mockito.never()).getBooksByTopic(Mockito.any(), Mockito.any());
    }

    @Test
    @DisplayName("previewSize 제약이 한쪽에만 선언돼 메서드 검증 메타데이터가 정상 빌드된다 (HV000151 회귀)")
    void methodValidationMetadata_buildsWithoutConstraintDeclarationException() throws Exception {
        ExecutableValidator validator = Validation.buildDefaultValidatorFactory()
                .getValidator()
                .forExecutables();
        TopicController controller = new TopicController(topicService);
        Method getAllTopics = TopicController.class.getMethod("getAllTopics", TopicTag.class, Integer.class);

        // 구현체와 인터페이스 양쪽에 파라미터 제약이 선언되어 있으면
        // 여기서 ConstraintDeclarationException(HV000151)이 터지고, 운영에서는 모든 요청이 500이 된다
        assertThatCode(() -> validator.validateParameters(controller, getAllTopics, new Object[]{null, 3}))
                .doesNotThrowAnyException();

        // 제약을 인터페이스로 옮긴 뒤에도 검증이 실제로 동작하는지 확인
        Set<ConstraintViolation<TopicController>> violations =
                validator.validateParameters(controller, getAllTopics, new Object[]{null, 0});
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("previewSize는 1 이상이어야 합니다.");
    }
}
