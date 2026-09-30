package whoreads.backend.domain.celebrity.entity;

import lombok.Getter;

/**
 * 인물 이미지의 저작권 라이선스 구분.
 * requiresAttribution()이 true인 이미지는 앱 내 출처 표기 화면에 반드시 노출되어야 한다.
 */
@Getter
public enum ImageLicense {

    /** 저작권 보호기간 만료 등으로 퍼블릭 도메인 */
    PUBLIC_DOMAIN("퍼블릭 도메인", false),

    /** CC0 - 저작권 포기 */
    CC0("CC0", false),

    /** CC BY - 저작자 표시 필요 */
    CC_BY("CC BY", true),

    /** CC BY-SA - 저작자 표시 + 동일조건 변경허락. 크롭 결과물도 동일 라이선스로 배포해야 하므로 신규 등록은 지양 */
    CC_BY_SA("CC BY-SA", true),

    /** 공공누리 제1유형 - 출처 표시 */
    KOGL_TYPE1("공공누리 제1유형", true),

    /** 직접 촬영/제작했거나 사용 허락을 받은 이미지 */
    OWNED("자체 보유", false),

    /** 라이선스 미확인. 배포 전 반드시 정리되어야 하는 상태 */
    UNKNOWN("미확인", true);

    private final String description;
    private final boolean requiresAttribution;

    ImageLicense(String description, boolean requiresAttribution) {
        this.description = description;
        this.requiresAttribution = requiresAttribution;
    }

    public boolean requiresAttribution() {
        return requiresAttribution;
    }
}
