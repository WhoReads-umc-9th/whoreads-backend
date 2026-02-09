# GitHub Secrets 설정 가이드

CI/CD 파이프라인 실행을 위해 아래 Secrets를 설정해야 합니다.

## 설정 방법
```
Repository → Settings → Secrets and variables → Actions → New repository secret
```

## 필수 Secrets 목록

### Docker Hub
| Name | Description | Example |
|------|-------------|---------|
| `DOCKER_USERNAME` | Docker Hub 사용자명 | `whoreads` |
| `DOCKER_PASSWORD` | Docker Hub Access Token | (토큰 값) |

> Docker Hub Access Token 발급: https://hub.docker.com/settings/security

### EC2 서버
| Name | Description | Example |
|------|-------------|---------|
| `EC2_HOST` | EC2 퍼블릭 IP 또는 도메인 | `43.201.xxx.xxx` |
| `EC2_USER` | EC2 SSH 사용자명 | `ubuntu` |
| `EC2_SSH_KEY` | EC2 SSH 프라이빗 키 (PEM 파일 내용 전체) | `-----BEGIN RSA PRIVATE KEY-----...` |

### RDS 데이터베이스
| Name | Description | Example |
|------|-------------|---------|
| `DB_HOST` | RDS 엔드포인트 | `whoreads.xxxxx.ap-northeast-2.rds.amazonaws.com` |
| `DB_PORT` | 데이터베이스 포트 | `3306` |
| `DB_NAME_STAGING` | Staging 환경 DB 이름 | `whoreads_staging` |
| `DB_NAME_PROD` | Production 환경 DB 이름 | `whoreads_prod` |
| `DB_USERNAME` | 데이터베이스 사용자명 | `admin` |
| `DB_PASSWORD` | 데이터베이스 비밀번호 | (비밀번호) |

### Firebase (FCM)
| Name | Description | Example |
|------|-------------|---------|
| `FIREBASE_SECRET_JSON_BASE64` | Firebase 서비스 계정 JSON 파일을 Base64 인코딩한 값 | (Base64 문자열) |
| `FIREBASE_SECRET_PATH` | Firebase 시크릿 파일 경로 (CI에서 자동 설정) | `./src/main/resources/firebase/secret-key.json` |

> Base64 인코딩 방법:
> ```bash
> base64 -i firebase-service-account.json | tr -d '\n'
> ```

## EC2 사전 준비사항

배포 전 EC2 서버에서 아래 작업이 필요합니다:

```bash
# 1. Docker 설치
sudo apt update
sudo apt install -y docker.io
sudo usermod -aG docker $USER

# 2. Docker 네트워크 생성
docker network create whoreads-network

# 3. 작업 디렉토리 생성
mkdir -p ~/whoreads

# 4. Nginx 설치 및 설정
sudo apt install -y nginx
```

### Nginx 설정 예시

**Staging** (`/etc/nginx/sites-available/staging`):
```nginx
server {
    listen 80;
    server_name staging.whoreads.com;

    location / {
        proxy_pass http://localhost:8081;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

**Production** (`/etc/nginx/sites-available/production`):
```nginx
server {
    listen 80;
    server_name whoreads.com;

    location / {
        proxy_pass http://localhost:8091;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

```bash
# 심볼릭 링크 생성
sudo ln -s /etc/nginx/sites-available/staging /etc/nginx/sites-enabled/
sudo ln -s /etc/nginx/sites-available/production /etc/nginx/sites-enabled/
sudo nginx -t && sudo systemctl reload nginx
```
