package whoreads.backend.domain.celebrity.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import whoreads.backend.global.entity.BaseEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "celebrity")
public class Celebrity extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String name;

    @Column(name = "image_url")
    private String imageUrl;
    
    // 한줄소개임
    @Column(name = "short_bio", nullable = false)
    private String shortBio;

    @Enumerated(EnumType.STRING)
    @Column(name = "tags", nullable = false)
    private CelebrityTag tags;
}