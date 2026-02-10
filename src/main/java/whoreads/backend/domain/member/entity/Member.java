package whoreads.backend.domain.member.entity;

import jakarta.persistence.*;
import lombok.*;
import whoreads.backend.domain.member.enums.AgeGroup;
import whoreads.backend.domain.member.enums.Gender;
import whoreads.backend.domain.member.enums.Status;
import whoreads.backend.global.entity.BaseEntity;

import java.time.LocalDateTime;

@Builder
@Entity
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table
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

    @Column(nullable = false, unique = true)
    private String loginId;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Status status = Status.ACTIVE;

    @Column
    private String dnaType;

    @Column
    private String dnaTypeName;

    @Column
    private String fcmToken;

    @Column
    private LocalDateTime fcmTokenUpdatedAt;

    private LocalDateTime deletedAt;

    public Status setStatus(Status status) {
        this.status = status;
        return status;
    }

    public void updateFcmToken(String fcmToken){
        this.fcmToken = fcmToken;
        this.fcmTokenUpdatedAt = LocalDateTime.now();
    }

    // 회원 탈퇴 (Soft Delete)
    public void withdraw() {
        this.status = Status.INACTIVE;
        this.deletedAt = LocalDateTime.now();
    }
}

