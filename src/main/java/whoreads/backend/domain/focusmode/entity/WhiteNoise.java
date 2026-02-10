package whoreads.backend.domain.focusmode.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import whoreads.backend.global.entity.BaseEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "white_noise")
public class WhiteNoise extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(name = "audio_url", columnDefinition = "TEXT", nullable = false)
    private String audioUrl;

    @Builder
    public WhiteNoise(String name, String audioUrl) {
        this.name = name;
        this.audioUrl = audioUrl;
    }
}
