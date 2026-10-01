package whoreads.backend.domain.celebrity.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CelebrityTest {

    @Test
    @DisplayName("편집된 이미지는 \"저작자 · 라이선스명 버전 · 편집됨\\n원본URL\" 형태로 조합된다")
    void imageAttribution_editedWithVersionAndSourceUrl() {
        Celebrity celebrity = Celebrity.builder()
                .name("홍길동")
                .shortBio("bio")
                .imageAuthor("Gil-dong Hong")
                .imageLicense(ImageLicense.CC_BY)
                .imageLicenseVersion("4.0")
                .imageSourceUrl("https://commons.wikimedia.org/wiki/File:example.jpg")
                .isEdited(true)
                .build();

        assertThat(celebrity.getImageAttribution())
                .isEqualTo("Gil-dong Hong · CC BY 4.0 · 편집됨\nhttps://commons.wikimedia.org/wiki/File:example.jpg");
    }

    @Test
    @DisplayName("편집하지 않은 이미지는 \"편집됨\" 표기를 생략한다")
    void imageAttribution_notEdited() {
        Celebrity celebrity = Celebrity.builder()
                .name("홍길동")
                .shortBio("bio")
                .imageAuthor("Gil-dong Hong")
                .imageLicense(ImageLicense.CC_BY)
                .imageLicenseVersion("4.0")
                .imageSourceUrl("https://commons.wikimedia.org/wiki/File:example.jpg")
                .isEdited(false)
                .build();

        assertThat(celebrity.getImageAttribution())
                .isEqualTo("Gil-dong Hong · CC BY 4.0\nhttps://commons.wikimedia.org/wiki/File:example.jpg");
    }

    @Test
    @DisplayName("버전 없는 라이선스(유튜브 CC BY 등)는 버전을 생략한다")
    void imageAttribution_withoutVersion() {
        Celebrity celebrity = Celebrity.builder()
                .name("홍길동")
                .shortBio("bio")
                .imageAuthor("Gil-dong Hong")
                .imageLicense(ImageLicense.CC_BY)
                .imageLicenseVersion(null)
                .build();

        assertThat(celebrity.getImageAttribution()).isEqualTo("Gil-dong Hong · CC BY");
    }

    @Test
    @DisplayName("원본 URL이 없으면 두 번째 줄을 생략한다")
    void imageAttribution_withoutSourceUrl() {
        Celebrity celebrity = Celebrity.builder()
                .name("홍길동")
                .shortBio("bio")
                .imageAuthor("Gil-dong Hong")
                .imageLicense(ImageLicense.CC_BY)
                .imageSourceUrl(null)
                .build();

        assertThat(celebrity.getImageAttribution()).isEqualTo("Gil-dong Hong · CC BY");
    }

    @Test
    @DisplayName("PUBLIC_DOMAIN/CC0는 저작자 유무와 무관하게 \"자유 이용 가능\"을 반환한다")
    void imageAttribution_publicDomainOrCc0() {
        Celebrity publicDomain = Celebrity.builder()
                .name("홍길동")
                .shortBio("bio")
                .imageAuthor("Gil-dong Hong")
                .imageLicense(ImageLicense.PUBLIC_DOMAIN)
                .build();
        Celebrity cc0 = Celebrity.builder()
                .name("홍길동")
                .shortBio("bio")
                .imageLicense(ImageLicense.CC0)
                .build();

        assertThat(publicDomain.getImageAttribution()).isEqualTo("자유 이용 가능");
        assertThat(cc0.getImageAttribution()).isEqualTo("자유 이용 가능");
    }

    @Test
    @DisplayName("출처 미확인(저작자 빈 문자열)은 라이선스가 UNKNOWN이면 빈 문자열을 반환한다 — 표기 불필요(null)와 구분")
    void imageAttribution_blankAuthor_returnsEmptyString() {
        Celebrity celebrity = Celebrity.builder()
                .name("홍길동")
                .shortBio("bio")
                .imageAuthor("")
                .imageLicense(ImageLicense.UNKNOWN)
                .build();

        assertThat(celebrity.getImageAttribution()).isEqualTo("");
    }

    @Test
    @DisplayName("라이선스 미지정(null)이고 저작자가 있으면 \"미확인\"으로 표기한다")
    void imageAttribution_nullLicense_withAuthor() {
        Celebrity celebrity = Celebrity.builder()
                .name("홍길동")
                .shortBio("bio")
                .imageAuthor("Gil-dong Hong")
                .imageLicense(null)
                .build();

        assertThat(celebrity.getImageAttribution()).isEqualTo("Gil-dong Hong · 미확인");
    }
}
