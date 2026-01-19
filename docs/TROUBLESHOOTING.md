# 🔍 WhoReads Troubleshooting Guide

이 문서는 프로젝트 개발 중 발생한 주요 기술적 이슈와 해결 방법을 기록합니다.

## 📋 기록 양식
| 일자 | 제목 | 관련 도메인 | 작성자 |
| :--- | :--- | :--- | :--- |
| 26-01-20 | [Docker] Spring Boot 컨테이너화 시 설정 충돌 | Infra | 김서연 |

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
