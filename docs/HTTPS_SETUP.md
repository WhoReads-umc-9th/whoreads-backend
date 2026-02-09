# Let's Encrypt HTTPS 설정 가이드

## 개요
`api.whoreads.kro.kr` 도메인에 Let's Encrypt SSL 인증서를 적용하는 가이드

## 사전 요구사항
- EC2 서버에 SSH 접속 가능
- 도메인(api.whoreads.kro.kr)이 EC2 IP를 가리키도록 DNS 설정 완료
- Docker 및 Nginx 컨테이너 실행 중

## 1. Certbot 설치 (EC2 서버)

```bash
# Ubuntu/Debian
sudo apt update
sudo apt install -y certbot

# Amazon Linux 2
sudo yum install -y certbot
```

## 2. 인증서 발급

### 방법 A: Standalone 모드 (Nginx 일시 중지 필요)

```bash
# Nginx 컨테이너 중지
docker stop whoreads-nginx

# 인증서 발급
sudo certbot certonly --standalone -d api.whoreads.kro.kr

# Nginx 컨테이너 재시작
docker start whoreads-nginx
```

### 방법 B: Webroot 모드 (Nginx 중지 없이)

```bash
# 1. Nginx 설정에 .well-known 경로 추가 (아래 3번 참고)
# 2. 인증서 발급
sudo certbot certonly --webroot -w /var/www/certbot -d api.whoreads.kro.kr
```

## 3. Nginx 설정 수정

### 디렉토리 구조
```
~/whoreads/
├── nginx/
│   └── conf.d/
│       └── default.conf
├── certbot/
│   └── www/              # Webroot 챌린지용
└── letsencrypt/          # 인증서 심볼릭 링크
```

### default.conf (HTTPS 적용)

```nginx
# HTTP → HTTPS 리다이렉트
server {
    listen 80;
    server_name api.whoreads.kro.kr;

    # Let's Encrypt 인증 챌린지용
    location /.well-known/acme-challenge/ {
        root /var/www/certbot;
    }

    # 나머지는 HTTPS로 리다이렉트
    location / {
        return 301 https://$host$request_uri;
    }
}

# HTTPS 서버
server {
    listen 443 ssl;
    server_name api.whoreads.kro.kr;

    # SSL 인증서
    ssl_certificate /etc/letsencrypt/live/api.whoreads.kro.kr/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/api.whoreads.kro.kr/privkey.pem;

    # SSL 설정 (권장)
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers ECDHE-ECDSA-AES128-GCM-SHA256:ECDHE-RSA-AES128-GCM-SHA256;
    ssl_prefer_server_ciphers off;
    ssl_session_cache shared:SSL:10m;
    ssl_session_timeout 1d;

    # Proxy to Spring Boot (Blue/Green)
    location / {
        proxy_pass http://whoreads-prod-blue:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

## 4. Nginx 컨테이너 재실행 (인증서 볼륨 마운트)

```bash
# 기존 Nginx 컨테이너 중지 및 삭제
docker stop whoreads-nginx
docker rm whoreads-nginx

# 인증서 볼륨 마운트하여 재실행
docker run -d \
  --name whoreads-nginx \
  --network whoreads-network \
  -p 80:80 \
  -p 443:443 \
  -v ~/whoreads/nginx/conf.d:/etc/nginx/conf.d:ro \
  -v /etc/letsencrypt:/etc/letsencrypt:ro \
  -v ~/whoreads/certbot/www:/var/www/certbot:ro \
  nginx:alpine
```

## 5. 인증서 자동 갱신 설정

### Cron 작업 등록

```bash
# crontab 편집
crontab -e

# 매일 새벽 3시에 갱신 시도 (추가)
0 3 * * * certbot renew --quiet --deploy-hook "docker exec whoreads-nginx nginx -s reload"
```

### 갱신 테스트

```bash
sudo certbot renew --dry-run
```

## 6. 확인

```bash
# HTTPS 접속 테스트
curl -I https://api.whoreads.kro.kr/api/health

# 인증서 정보 확인
echo | openssl s_client -connect api.whoreads.kro.kr:443 2>/dev/null | openssl x509 -noout -dates
```

## 트러블슈팅

### 인증서 발급 실패
- DNS가 EC2 IP를 가리키는지 확인: `nslookup api.whoreads.kro.kr`
- 80 포트가 열려있는지 확인: EC2 보안 그룹 확인
- 방화벽 확인: `sudo ufw status` 또는 `sudo iptables -L`

### Nginx 시작 실패
- 인증서 경로 확인: `ls -la /etc/letsencrypt/live/api.whoreads.kro.kr/`
- 설정 문법 확인: `docker exec whoreads-nginx nginx -t`

### 인증서 갱신 실패
- 갱신 로그 확인: `sudo cat /var/log/letsencrypt/letsencrypt.log`
- 수동 갱신 시도: `sudo certbot renew --force-renewal`
