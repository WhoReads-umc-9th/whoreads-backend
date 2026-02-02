package whoreads.backend.domain.topic.entity; // 👈 entity 패키지 추가

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import whoreads.backend.global.entity.BaseEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "topic")
public class Topic extends BaseEntity { // 주제

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "topic_id")
    private Long id;

    @Column(nullable = false, unique = true)
    private String name; // 예: "번아웃", "창업 초기"

    private String description; // 예: "지친 마음을 위로하는 책들"

    @Builder
    public Topic(String name, String description) {
        this.name = name;
        this.description = description;
    }
}