# 📚 WhoReads (Backend)
> **“유명인의 독서 맥락을 통해 독서를 부담이 아닌 ‘지속되는 독서 루틴’으로 바꿔주는 서비스”**

WhoReads는 유명인의 실제 발언과 인용을 기반으로 독서 동기를 부여하고, 개인별 독서 DNA에 맞는 롤모델을 매칭해주는 **인물 중심 독서 큐레이션 플랫폼**입니다.

---

## 🛠 Tech Stack
- **Language**: Java 17 (Azul Zulu)
- **Framework**: Spring Boot 3.x
- **Build Tool**: Gradle
- **Database**: MySQL 8.0, Redis (Refresh Token & Cache)
- **ORM**: Spring Data JPA
- **API Documentation**: Swagger (SpringDoc UI)
- **Infrastructure**: AWS EC2, RDS, S3

---

## ✨ Core Features & Logic
서비스의 핵심 비즈니스 로직 및 백엔드 구현 포인트입니다.

1. **증거 기반 인용 & 딥링크**
   - 유명인의 실제 발언 데이터와 원문 출처(유튜브, 트위터 등)를 연결하는 데이터 스키마 설계.
2. **독서 DNA 테스트 (MBTI 기반)**
   - 유저의 응답을 기반으로 `DnaMatchService`에서 알고리즘 연산을 통해 유사한 유명인 페르소나 매칭.
3. **개인 서재 및 상태 관리**
   - **4-Step 상태**: `WISH`, `READING`, `DONE`, `DROPPED`
   - **Drop It**: 도서 중단 시 단순 삭제가 아닌 `DROPPED` 상태값 변경을 통해 유저의 취향 탐색 이력 보존.
4. **유명인 싱크로율 대시보드**
   - 유저의 독서 목록과 팔로우한 유명인의 추천 목록 간의 교집합(Intersection)을 연산하여 % 시각화.
5. **맥락 기반 스낵 알림**
   - 설정된 시간에 맞춰 유명인의 인용구와 책 정보를 포함한 Push 알림 발송 로직.

---

## 📂 Architecture & Directory Structure
표준적인 **Layered Architecture**를 따르며, 도메인 주도 설계를 지향합니다.

다음은 예시 프로젝트 구조입니다. (세부 폴더 이름 등등 확정 X!!)

```text
src/main/java/com/whoreads/
├── domain/
│   ├── member/          # 회원 가입, 로그인, 마이페이지
│   ├── celebrity/       # 유명인 프로필, 가상 서재, 실제 발언 인용
│   ├── book/            # 도서 정보, 주제별 큐레이션, 추천 네트워크
│   ├── dna/             # 독서 DNA 테스트, 롤모델 매칭 알고리즘
│   └── notification/    # 관계 기반 스낵 알림, 알림 이력
│
├── global/              # 도메인 전반에 걸친 공통 요소
│   ├── common/          # 공통 응답 객체, BaseEntity
│   ├── config/          # Security, Swagger, Querydsl 설정
│   ├── error/           # GlobalExceptionHandler, CustomException
│   └── util/            # JwtTokenProvider, 날짜 유틸 등
│
└── infra/               # 외부 인프라스트럭처 계층
    ├── s3/              # 이미지 업로드 관련
    └── fcm/             # 푸시 알림 서버 연동
```

---
## 📖 Project Documentation
프로젝트 운영 및 기술 관련 상세 정보는 아래 문서를 참고하세요.

| 문서명 | 주요 내용 | 바로가기 |
| :--- | :--- | :---: |
| **🤝 Collaboration Rules** | 브랜치 전략, 커밋 컨벤션, PR 규칙 | [바로가기](./COLLABORATION.md) |
| **📝 API Specification** | 도메인별 API 엔드포인트 및 응답 구조 | [바로가기](./API_SPEC.md) |
| **🗄️ Local DB Setup** | 로컬 DB 설정 (MySQL 직접 설치 / Docker) | [바로가기](./docs/LOCAL_DB_SETUP.md) |
| **🔍 Troubleshooting** | 개발 중 발생한 이슈 및 해결 방법 기록 | [바로가기](./TROUBLESHOOTING.md) |

---
## 👥 Team Members

| Profile | Name | Role | Contributions | GitHub |
| :---: | :--- | :--- | :--- | :---: |
| <img src="https://github.com/seoyeoki.png" width="100"> | **냠냠** | **Backend** | • 프로젝트 아키텍처 설계<br>• 독서 DNA 매칭 알고리즘 구현<br>• 데이터 소스 정제 및 관리 | [@member1](https://github.com/member2) |
| <img src="https://github.com/member2.png" width="100"> | **팀원 2** | **Backend** | • 인물 중심 가상 서재 API<br>• 4-Step 상태 관리 및 Drop It 로직<br>• DB 스키마 설계 | [@member2](https://github.com/member2) |
| <img src="https://github.com/member3.png" width="100"> | **팀원 3** | **Backend** | • 유명인 인용 기반 스와이프 UI<br>• 개인 서재 대시보드 시각화<br>• 웹 앱 반응형 인터페이스 구현 | [@member3](https://github.com/member3) |
| <img src="https://github.com/member4.png" width="100"> | **김서연** | **Backend / Infra** | • 관계 기반 스낵 알림 시스템<br>• 팩트체크 딥링크 연동 모듈<br>• 서버 인프라 구축(AWS) 및 CI/CD 파이프라인 구축 | [@seoyeoki](https://github.com/seoyeoki) |

- **김서연**: 깃허브/배포 관리
- **주요 기능 구현**: 어쩌구...(예시)

---
