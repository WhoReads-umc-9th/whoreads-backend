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
    private String name;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "topic_tags",
            joinColumns = @JoinColumn(name = "topic_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "tag")
    private List<TopicTag> tags = new ArrayList<>();

    public Topic(String name, List<TopicTag> tags) {
        this.name = name;
        this.tags = (tags != null) ? tags : new ArrayList<>();
    }
}