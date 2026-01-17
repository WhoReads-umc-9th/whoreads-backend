# 로컬 데이터베이스 설정 가이드

로컬 개발 환경에서 데이터베이스를 설정하는 방법을 안내합니다.
**두 가지 방법 중 하나를 선택**하여 진행하세요.

| 방식 | 장점 | 단점 |
| :--- | :--- | :--- |
| **MySQL 직접 설치** | 별도 도구 불필요, 가벼움 | OS별 설치 방법 상이, 버전 관리 어려움 |
| **Docker 사용** | 환경 통일, 간편한 초기화 | Docker 설치 필요, 리소스 사용 |

---

## 공통: application-local.yml 생성

`application-local.yml` 파일은 `.gitignore`에 포함되어 있어 GitHub에 업로드되지 않습니다.
아래 내용을 복사하여 **`src/main/resources/application-local.yml`** 파일을 새로 생성하세요.

### application-local.yml 샘플

```yaml
spring:
  datasource:
    url: jdbc:mysql://${DB_HOST}:${DB_PORT}/${DB_NAME}?useSSL=false&serverTimezone=Asia/Seoul&characterEncoding=UTF-8&allowPublicKeyRetrieval=true
    username: ${USER_NAME}
    password: ${USER_PASSWORD}
    driver-class-name: com.mysql.cj.jdbc.Driver
```

---

## 방법 1: MySQL 직접 설치

로컬 PC에 MySQL을 직접 설치하여 사용하는 방법입니다.

### 1-1. MySQL 설치

**macOS (Homebrew):**
```bash
brew install mysql
brew services start mysql
```

**Windows:**
1. [MySQL Community Downloads](https://dev.mysql.com/downloads/mysql/) 접속
2. MySQL Installer 다운로드 및 실행
3. Developer Default 또는 Server Only 선택하여 설치

**Linux (Ubuntu/Debian):**
```bash
sudo apt update
sudo apt install mysql-server
sudo systemctl start mysql
```

### 1-2. 데이터베이스 및 계정 생성

MySQL에 접속하여 데이터베이스와 계정을 생성합니다.

```bash
# MySQL 접속 (root 계정)
mysql -u root -p
```

```sql
-- 1. 데이터베이스 생성
CREATE DATABASE IF NOT EXISTS whoreads;

-- 2. 계정 생성 (원하는 계정명/비밀번호로 변경)
CREATE USER IF NOT EXISTS 'your_username'@'localhost' IDENTIFIED BY 'your_password';

-- 3. 권한 부여
GRANT ALL PRIVILEGES ON whoreads.* TO 'your_username'@'localhost';

-- 4. 변경사항 적용
FLUSH PRIVILEGES;
```

### 1-3. 환경변수 설정

**직접 생성한 계정 정보**를 환경변수에 설정합니다.

| 환경변수 | 설명 | 예시 값 |
| :--- | :--- | :--- |
| `DB_HOST` | 데이터베이스 호스트 | `localhost` |
| `DB_PORT` | 데이터베이스 포트 | `3306` |
| `DB_NAME` | 데이터베이스 이름 | `whoreads` |
| `USER_NAME` | 데이터베이스 계정명 | *직접 생성한 계정명 입력* |
| `USER_PASSWORD` | 데이터베이스 비밀번호 | *직접 생성한 비밀번호 입력* |

**IntelliJ IDEA 환경변수 설정:**
1. Run → Edit Configurations
2. Environment variables 항목에 추가:
   ```
   DB_HOST=localhost;DB_PORT=3306;DB_NAME=whoreads;USER_NAME=your_username;USER_PASSWORD=your_password
   ```

---

## 방법 2: Docker 사용

Docker를 활용하여 MySQL 컨테이너를 실행하는 방법입니다.

### Docker를 사용하는 이유

1. **환경 통일**: 모든 팀원이 동일한 MySQL 버전(8.0)과 설정으로 개발
2. **간편한 초기화**: 컨테이너 삭제 후 재생성으로 깨끗한 DB 환경 복구
3. **자동 설정**: `docker-compose.yml` 실행만으로 DB, 계정, 권한이 자동 생성
4. **충돌 방지**: 로컬에 설치된 다른 MySQL과 독립적으로 운영

### 2-1. Docker 설치

**macOS:**
```bash
# Homebrew 사용
brew install --cask docker

# 또는 Docker Desktop 직접 다운로드
# https://www.docker.com/products/docker-desktop/
```
설치 후 Docker Desktop 앱을 실행하여 Docker 엔진을 시작합니다.

**Windows:**
1. [Docker Desktop for Windows](https://www.docker.com/products/docker-desktop/) 다운로드
2. 설치 파일 실행 및 설치 완료
3. WSL 2 백엔드 사용 권장 (설치 중 옵션 선택)
4. Docker Desktop 실행

**Linux (Ubuntu/Debian):**
```bash
# Docker 설치
sudo apt update
sudo apt install docker.io docker-compose

# Docker 서비스 시작
sudo systemctl start docker
sudo systemctl enable docker

# 현재 사용자를 docker 그룹에 추가 (sudo 없이 사용)
sudo usermod -aG docker $USER
# 터미널 재시작 필요
```

### 2-2. Docker Compose로 MySQL 실행

프로젝트 루트에 있는 `docker-compose.yml` 파일을 사용합니다.

```bash
# 프로젝트 루트 디렉토리에서 실행

# 컨테이너 시작 (백그라운드)
docker-compose up -d

# 컨테이너 상태 확인
docker-compose ps

# 로그 확인
docker-compose logs -f db

# 컨테이너 중지
docker-compose down

# 컨테이너 및 데이터 삭제 (초기화)
docker-compose down -v
rm -rf ./db/data
```

### 2-3. 자동 생성되는 리소스

`docker-compose.yml` 실행 시 `db/initdb/01_init.sql` 스크립트가 자동 실행되어 다음이 생성됩니다:

- **데이터베이스**: `whoreads`
- **계정**: `tiger` / `tiger1234!`
- **권한**: `whoreads` 데이터베이스에 대한 모든 권한

> **참고**: 기본 계정은 `tiger` / `tiger1234!`로 생성됩니다.
> 필요시 `db/initdb/01_init.sql` 파일을 수정하여 계정 정보를 변경할 수 있습니다.

### 2-4. 환경변수 설정

Docker 사용 시 아래 값을 환경변수에 설정합니다.

| 환경변수 | 설명 | 값 |
| :--- | :--- | :--- |
| `DB_HOST` | 데이터베이스 호스트 | `localhost` |
| `DB_PORT` | 데이터베이스 포트 | `3306` |
| `DB_NAME` | 데이터베이스 이름 | `whoreads` |
| `USER_NAME` | 데이터베이스 계정명 | `tiger` |
| `USER_PASSWORD` | 데이터베이스 비밀번호 | `tiger1234!` |

**IntelliJ IDEA 환경변수 설정:**
1. Run → Edit Configurations
2. Environment variables 항목에 추가:
   ```
   DB_HOST=localhost;DB_PORT=3306;DB_NAME=whoreads;USER_NAME=tiger;USER_PASSWORD=tiger1234!
   ```

---

## 문제 해결

### MySQL 연결 오류

**증상**: `Communications link failure` 또는 `Connection refused`

**해결**:
1. MySQL/Docker 컨테이너가 실행 중인지 확인
2. 포트(3306)가 다른 프로세스에서 사용 중인지 확인
   ```bash
   lsof -i :3306
   ```

### Docker 컨테이너 시작 실패

**증상**: `docker-compose up` 실행 시 오류

**해결**:
1. Docker Desktop이 실행 중인지 확인
2. 기존 데이터 충돌 시 초기화:
   ```bash
   docker-compose down -v
   rm -rf ./db/data
   docker-compose up -d
   ```

### 계정 권한 오류

**증상**: `Access denied for user`

**해결**:
1. 환경변수의 계정명/비밀번호가 정확한지 확인
2. Docker 사용 시: `tiger` / `tiger1234!`
3. MySQL 직접 설치 시: 직접 생성한 계정 정보 확인
