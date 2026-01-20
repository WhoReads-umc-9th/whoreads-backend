# Docker로 Spring Boot 실행하기

Spring Boot 애플리케이션을 Docker 컨테이너로 실행하는 방법을 안내합니다.

> **참고**: Spring Boot는 **꼭 Docker로 실행할 필요가 없습니다.**
> 로컬 개발 시에는 DB만 Docker로 띄우고, 앱은 IDE에서 직접 실행하는 것이 더 빠르고 편리합니다.
> Docker로 앱까지 실행하는 것은 **배포 환경 테스트**나 **통합 테스트** 시 유용합니다.

---

## 파일 구성

| 파일 | 용도 |
| :--- | :--- |
| `docker-compose.yml` | DB만 실행 (로컬 개발용) |
| `docker-compose.app.yml` | 앱만 실행 (통합 테스트/배포용, 외부 DB 연결) |
| `Dockerfile` | Spring Boot 앱 이미지 빌드 |
| `.env` | 환경변수 설정 (DB 연결 정보, 프로필 등) |

---

## 실행 방법

### 1. 로컬 개발 (권장)

DB만 Docker로 실행하고, Spring Boot는 IDE에서 실행합니다.

```bash
# DB 컨테이너 시작
docker-compose up -d

# DB 컨테이너 중지
docker-compose down
```

이후 IntelliJ 등 IDE에서 Spring Boot 애플리케이션을 실행하세요.

### 2. 전체 Docker 실행

Spring Boot 앱을 Docker로 실행합니다.

> **중요**: 실행 전 `.env` 파일에 `APP_PROFILE` 환경변수를 설정해야 합니다.
> - 로컬 개발: `APP_PROFILE=local`
> - 운영 환경: `APP_PROFILE=prod`

```bash
# .env 파일 설정 (로컬 환경 예시)
APP_PROFILE=local
DB_HOST=host.docker.internal
DB_PORT=3306
DB_NAME=whoreads
USER_NAME=tiger
USER_PASSWORD=tiger1234!
```

```bash
# 앱 실행
docker-compose -f docker-compose.app.yml up -d

# 로그 확인
docker-compose -f docker-compose.app.yml logs -f app

# 전체 중지
docker-compose -f docker-compose.app.yml down
```

### 3. 앱 이미지 재빌드

코드 변경 후 이미지를 새로 빌드해야 할 때:

```bash
# 이미지 재빌드 후 실행
docker-compose -f docker-compose.app.yml up -d --build
```

---

## 컨테이너 상태 확인

```bash
# 실행 중인 컨테이너 확인
docker ps

# 컨테이너 로그 확인
docker logs whoreads-app
docker logs whoreads-local-db
```

---

## 문제 해결

### 앱이 DB에 연결하지 못할 때

**증상**: `Communications link failure` 또는 `Connection refused`

**해결**:
1. DB 컨테이너가 healthy 상태인지 확인
   ```bash
   docker ps
   # STATUS에 (healthy) 표시 확인
   ```
2. DB가 완전히 시작될 때까지 대기 (약 30초)

### 포트 충돌

**증상**: `Bind for 0.0.0.0:8080 failed: port is already allocated`

**해결**:
1. 로컬에서 실행 중인 Spring Boot 종료
2. 또는 사용 중인 포트 확인 후 종료
   ```bash
   lsof -i :8080
   kill -9 <PID>
   ```

### 이미지 재빌드가 반영되지 않을 때

```bash
# 캐시 없이 완전히 새로 빌드
docker-compose -f docker-compose.app.yml build --no-cache
docker-compose -f docker-compose.app.yml up -d
```
