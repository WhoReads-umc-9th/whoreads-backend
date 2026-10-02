package whoreads.backend.domain.celebrity.entity;

import jakarta.persistence.*;
import lombok.*;
import whoreads.backend.global.entity.BaseEntity;

import java.util.ArrayList;
import java.util.List;

@Builder
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "celebrity")
public class Celebrity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name; // 추천인 이름

    @Column(name = "image_url", columnDefinition = "TEXT")
    private String imageUrl; // S3 objectKey (예: celebrity/12/profile_4x5_v1.webp). 과도기에는 전체 URL도 허용

    @Column(name = "image_source_url", columnDefinition = "TEXT")
    private String imageSourceUrl; // 이미지를 가져온 원본 페이지 URL

    @Column(name = "image_author", length = 100)
    private String imageAuthor; // 사진 저작자 표기명

    @Enumerated(EnumType.STRING)
    @Column(name = "image_license", length = 30)
    private ImageLicense imageLicense; // 저작권 라이선스 계열 (CC_BY, CC_BY_SA 등)

    @Column(name = "image_license_version", length = 10)
    private String imageLicenseVersion; // 라이선스 버전 (예: "2.0", "3.0", "4.0"). 같은 계열이어도
    // 버전마다 별개 법적 문서라 저작자 표시 문구에 반드시 함께 노출해야 한다.
    // 유튜브 CC BY처럼 버전 개념이 없는 출처는 null로 둔다.

    @Column(name = "is_edited", nullable = false)
    private boolean isEdited; // 원본을 크롭 등으로 변형했는지 여부. CC BY/BY-SA는 변형 시 표시 의무가 있어 출처표시 문구에 반영한다.

    @Column(name = "short_bio", nullable = false)
    private String shortBio; // 한줄 소개

    @Column(columnDefinition = "TEXT")
    private String resultComment;

    // 직업 태그 (가수, 배우 등) - 필터링용
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "celebrity_job_tags",
            joinColumns = @JoinColumn(name = "celebrity_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "job_tag")
    private List<CelebrityTag> jobTags = new ArrayList<>();

    @OneToMany(mappedBy = "celebrity", cascade = CascadeType.ALL)
    @Builder.Default
    private List<CelebrityBook> celebrityBookList = new ArrayList<>();

    /**
     * 앱에 그대로 표시할 출처 관련 문구. 세 가지 경우로 나뉜다:
     * - "자유 이용 가능": 저작권이 없거나 포기된 경우(PUBLIC_DOMAIN, CC0) — 표시 의무는 없지만 상태는 알려준다
     * - "{저작자} · {라이선스} {버전} · 편집됨\n{원본 URL}": 표시 의무가 있고 저작자 정보가 있는 경우
     * - "" (빈 문자열): 표시 의무는 있는데 저작자 정보가 없는 경우(출처 미확인 13명) —
     *   의무 자체가 없는 경우와 구분해 "확인 필요" 상태를 프론트가 식별할 수 있게 한다.
     */
    public String getImageAttribution() {
        if (imageLicense == ImageLicense.PUBLIC_DOMAIN || imageLicense == ImageLicense.CC0) {
            return "자유 이용 가능";
        }
        if (imageAuthor == null || imageAuthor.isBlank()) {
            return "";
        }

        String licenseDescription = imageLicense != null ? imageLicense.getDescription() : "미확인";
        StringBuilder sb = new StringBuilder(imageAuthor).append(" · ").append(licenseDescription);
        if (imageLicenseVersion != null && !imageLicenseVersion.isBlank()) {
            sb.append(" ").append(imageLicenseVersion);
        }
        if (isEdited) {
            sb.append(" · 편집됨");
        }
        if (imageSourceUrl != null && !imageSourceUrl.isBlank()) {
            sb.append("\n").append(imageSourceUrl);
        }
        return sb.toString();
    }

    @Builder
    public Celebrity(String name, String imageUrl, String shortBio, List<CelebrityTag> jobTags,
                     String imageSourceUrl, String imageAuthor, ImageLicense imageLicense,
                     String imageLicenseVersion, boolean isEdited) {
        this.name = name;
        this.imageUrl = imageUrl;
        this.shortBio = shortBio;
        this.jobTags = (jobTags != null) ? jobTags : new ArrayList<>();
        this.imageSourceUrl = imageSourceUrl;
        this.imageAuthor = imageAuthor;
        this.imageLicense = imageLicense;
        this.imageLicenseVersion = imageLicenseVersion;
        this.isEdited = isEdited;
    }
}
