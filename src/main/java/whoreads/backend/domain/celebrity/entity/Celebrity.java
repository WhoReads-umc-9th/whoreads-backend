package whoreads.backend.domain.celebrity.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import whoreads.backend.global.entity.BaseEntity;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "celebrity")
public class Celebrity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name; // 추천인 이름

    @Column(name = "image_url", columnDefinition = "TEXT")
    private String imageUrl; // 이미지 URL

    @Column(name = "short_bio", nullable = false)
    private String shortBio; // 한줄 소개

    // 직업 태그 (가수, 배우 등) - 필터링용
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "celebrity_job_tags",
            joinColumns = @JoinColumn(name = "celebrity_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "job_tag")
    private List<CelebrityTag> jobTags = new ArrayList<>();

    @Builder
    public Celebrity(String name, String imageUrl, String shortBio, List<CelebrityTag> jobTags) {
        this.name = name;
        this.imageUrl = imageUrl;
        this.shortBio = shortBio;
        this.jobTags = (jobTags != null) ? jobTags : new ArrayList<>();
    }
}
