package whoreads.backend.global.converter;

import org.springframework.core.convert.converter.Converter;
import whoreads.backend.domain.topic.entity.TopicTag;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * 요청의 문자열을 TopicTag로 변환한다.
 * <p>
 * 추가 이유: 기본 enum 변환은 상수명이 정확히 일치해야만 통과해서, 프론트가 소문자("mindset")나
 * 응답으로 받은 한글 이름("사고방식"), 하이픈 표기("top-20")를 넘기면 전부 변환 실패로 떨어졌다.
 * 프론트 수정 없이도 동작하도록 표기 차이를 흡수한다.
 * <p>
 * 매칭에 실패하면 IllegalArgumentException을 던진다. Spring이 이를
 * MethodArgumentTypeMismatchException으로 감싸고, GlobalExceptionHandler가 허용값을 안내하는
 * 400으로 응답한다.
 */
public class StringToTopicTagConverter implements Converter<String, TopicTag> {

    // 대소문자와 구분자(_, -, 공백, 가운뎃점) 차이를 무시하고 비교하기 위한 정규화 대상
    private static final char[] SEPARATORS = {' ', '\t', '_', '-', '·', 'ㆍ'};

    // 상수명과 한글 description 양쪽을 정규화한 키로 등록해둔다
    private static final Map<String, TopicTag> LOOKUP = new HashMap<>();

    static {
        for (TopicTag tag : TopicTag.values()) {
            LOOKUP.put(normalize(tag.name()), tag);
            LOOKUP.put(normalize(tag.getDescription()), tag);
        }
    }

    @Override
    public TopicTag convert(String source) {
        // 기존 동작 유지: ?tag= 처럼 빈 값은 "필터 없음"(null)으로 취급
        if (source == null || source.isBlank()) {
            return null;
        }

        TopicTag tag = LOOKUP.get(normalize(source));
        if (tag == null) {
            throw new IllegalArgumentException("지원하지 않는 주제값입니다: " + source);
        }
        return tag;
    }

    private static String normalize(String value) {
        String normalized = value.toUpperCase(Locale.ROOT);
        for (char separator : SEPARATORS) {
            normalized = normalized.replace(String.valueOf(separator), "");
        }
        return normalized;
    }
}
