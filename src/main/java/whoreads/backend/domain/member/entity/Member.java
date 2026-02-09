package whoreads.backend.domain.member.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import whoreads.backend.global.entity.BaseEntity;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AgeGroup ageGroup;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column
    private String dnaType;

    @Column
    private String dnaTypeName;

    @Column()
    private String fcmToken;

    @Column()
    private LocalDateTime fcmTokenUpdatedAt;

    @Builder
    public Member(String nickname, Gender gender, AgeGroup ageGroup,
                  String email, String password, String dnaType, String dnaTypeName) {
        this.nickname = nickname;
        this.gender = gender;
        this.ageGroup = ageGroup;
        this.email = email;
        this.password = password;
        this.dnaType = dnaType;
        this.dnaTypeName = dnaTypeName;
    }
}
