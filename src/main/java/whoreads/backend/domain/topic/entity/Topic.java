package whoreads.backend.domain.topic.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "topic")
public class Topic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;        // 예: 각 분야의 유명인들이 사회를 이해하기 위해 읽은 책

    @Column(nullable = false)
    private String description; // 예: 세상은 왜 이렇게 돌아갈까?

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "topic_tags",
            joinColumns = @JoinColumn(name = "topic_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "tag")
    private List<TopicTag> tags = new ArrayList<>();

    public Topic(String name, String description, List<TopicTag> tags) {
        this.name = name;
        this.description = description;
        this.tags = (tags != null) ? tags : new ArrayList<>();
    }
}