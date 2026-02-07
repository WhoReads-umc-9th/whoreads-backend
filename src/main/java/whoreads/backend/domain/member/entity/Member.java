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
@Getter
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
    @Column
    private Status status;

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


//    @Builder
//    public Member(String nickname, Gender gender, AgeGroup ageGroup,
//                  String email, String password, String dnaType, String dnaTypeName) {
//        this.nickname = nickname;
//        this.gender = gender;
//        this.ageGroup = ageGroup;
//        this.email = email;
//        this.password = password;
//        this.dnaType = dnaType;
//        this.dnaTypeName = dnaTypeName;
//    }
    public void updateFcmToken(String fcmToken){
        this.fcmToken = fcmToken;
        this.fcmTokenUpdatedAt = LocalDateTime.now();
    }
}

