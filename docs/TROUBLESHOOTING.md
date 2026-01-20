# 🔍 WhoReads Troubleshooting Guide

이 문서는 프로젝트 개발 중 발생한 주요 기술적 이슈와 해결 방법을 기록합니다.

## 📋 기록 양식
| 일자 | 제목 | 관련 도메인 | 작성자 |
| :--- | :--- | :--- | :--- |
| 26-01-20 | [Docker] Spring Boot 컨테이너화 시 설정 충돌 | Infra | 김서연 |
| 26-01-20 | [Docker] 환경변수 주입 및 Spring Profile 동적 할당 | Infra | 김서연 |
| 26-01-20 | [Docker] docker-compose 환경변수 위치 오류 | Infra | 김서연 |

---

## 📑 주요 이슈 내역

### [Issue #01] Health Check API 401 Unauthorized 에러
- **현상**: `GET /api/health` 요청 시 401 Unauthorized 에러 발생
- **원인**: `spring-boot-starter-security` 의존성이 포함되어 있으나 `SecurityConfig`가 없어서 모든 엔드포인트에 인증 필요
- **해결**: `SecurityConfig.java` 추가하여 `/api/health` 엔드포인트를 `permitAll()`로 설정
```java
.authorizeHttpRequests(auth -> auth
    .requestMatchers("/api/health").permitAll()
    .anyRequest().authenticated()
)
```

### [Issue #02] Docker Alpine 이미지 호환성 에러
- **현상**: `eclipse-temurin:17-jre-alpine` 이미지 사용 시 런타임 에러 발생
- **원인**: Alpine Linux는 `musl libc`를 사용하는데, 일부 Java 라이브러리가 `glibc`에 의존
- **해결**: Alpine 대신 일반 이미지 사용
```dockerfile
# 변경 전
FROM eclipse-temurin:17-jre-alpine

# 변경 후
FROM eclipse-temurin:21-jre
```

### [Issue #03] Gradle 버전 호환성 에러
- **현상**: Docker 빌드 시 `Could not find method toolchain()` 또는 Spring Boot 플러그인 버전 에러
- **원인**: Docker 이미지의 Gradle 8.5와 프로젝트가 요구하는 Gradle 8.14+ 버전 불일치
- **해결**: 시스템 Gradle 대신 프로젝트의 Gradle Wrapper 사용
```dockerfile
# 변경 전
FROM gradle:8.5-jdk17 AS builder
RUN gradle bootJar --no-daemon -x test

# 변경 후
FROM eclipse-temurin:21-jdk AS builder
COPY gradlew ./
COPY gradle ./gradle
RUN chmod +x ./gradlew
RUN ./gradlew bootJar --no-daemon -x test
```

### [Issue #04] Java 버전 불일치 에러
- **현상**: Docker 빌드 시 `languageVersion=21`을 찾을 수 없다는 에러 발생
- **원인**: 프로젝트는 Java 21을 사용하는데 Dockerfile의 베이스 이미지가 Java 17
- **해결**: Dockerfile의 베이스 이미지를 Java 21로 변경
```dockerfile
# 변경 전
FROM eclipse-temurin:17-jdk AS builder
FROM eclipse-temurin:17-jre

# 변경 후
FROM eclipse-temurin:21-jdk AS builder
FROM eclipse-temurin:21-jre
```

### [Issue #05] Docker 환경변수 주입 및 Spring Profile 동적 할당
- **현상**: Docker Compose를 통해 Spring Boot 애플리케이션 실행 시 DataSource 설정 누락으로 컨테이너 시작 실패
  ```text
  Failed to configure a DataSource: 'url' attribute is not specified and no embedded datasource could be configured.
  Reason: Failed to determine a suitable driver class
  ```
- **원인**:
  1. `application.yml`에 `profiles.include: local`이 하드코딩되어 있어 Docker 환경에서 프로필 동적 변경 불가
  2. Docker Compose의 `env_file`만으로는 Spring Boot가 환경변수를 인식하지 못함
- **해결**:
  1. `application.yml`에서 `profiles.include: local` 제거
  2. `docker-compose.app.yml`에 환경변수 명시적 추가
  ```yaml
  environment:
    SPRING_PROFILES_ACTIVE: ${SPRING_PROFILES_ACTIVE}
    DB_HOST: ${DB_HOST}
    DB_PORT: ${DB_PORT}
    DB_NAME: ${DB_NAME}
    USER_NAME: ${USER_NAME}
    USER_PASSWORD: ${USER_PASSWORD}
  ```
  3. `.env` 파일에 `SPRING_PROFILES_ACTIVE=local` (로컬) 또는 `SPRING_PROFILES_ACTIVE=prod` (운영) 설정

### [Issue #06] docker-compose 환경변수 위치 오류
- **현상**: `SPRING_PROFILES_ACTIVE` 환경변수가 무시되고 default 프로필로 실행됨
  ```text
  No active profile set, falling back to 1 default profile: "default"
  Failed to configure a DataSource: 'url' attribute is not specified...
  ```
- **원인**: `docker-compose.app.yml`에서 `env_file`과 `environment` 블록이 `app` 서비스가 아닌 `nginx` 서비스에 잘못 위치
  ```yaml
  # 잘못된 구조
  services:
    app:
      ...
    nginx:
      ...
      env_file:      # ← nginx에 붙어있음!
      environment:   # ← nginx에 붙어있음!
  ```
- **해결**: 환경변수 블록을 `app` 서비스로 이동
  ```yaml
  services:
    app:
      ...
      env_file:
        - .env
      environment:
        SPRING_PROFILES_ACTIVE: ${SPRING_PROFILES_ACTIVE}
        DB_HOST: ${DB_HOST}
        ...
    nginx:
      ...
  ```
