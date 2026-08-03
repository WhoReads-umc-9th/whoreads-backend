# Let's Encrypt HTTPS 설정 가이드

## 개요
`api.whoreads.kro.kr` 도메인에 Let's Encrypt SSL 인증서를 적용하는 가이드

## 사전 요구사항
- EC2 서버에 SSH 접속 가능
- 도메인(api.whoreads.kro.kr)이 EC2 IP를 가리키도록 DNS 설정 완료
- Docker 및 Nginx 컨테이너 실행 중 (`docker-compose.yml`의 `whoreads-nginx`)

## 1. Certbot 설치 (EC2 서버)

```bash
# Ubuntu/Debian
sudo apt update
sudo apt install -y certbot

# Amazon Linux 2
sudo yum install -y certbot
```

apt로 설치하면 `certbot.timer`(systemd, 하루 2회 실행)가 자동으로 등록되어
별도의 cron 등록 없이도 만료 30일 전부터 자동 갱신을 시도합니다.

## 2. 인증서 발급 — Webroot 모드만 사용

> ⚠️ **Standalone 모드(`certbot certonly --standalone`)는 사용하지 마세요.**
> `whoreads-nginx` 컨테이너가 80/443 포트를 상시 점유하고 있어서, standalone이
> 갱신 시 자체적으로 80번 포트를 바인딩하려다 `Could not bind TCP port 80`
> 에러로 실패합니다. 이 저장소는 nginx를 내리지 않는 **webroot 모드만** 지원합니다.

```bash
# 최초 발급 시 (webroot 디렉토리는 docker-compose.yml에 이미 마운트되어 있음)
sudo certbot certonly --webroot -w ~/whoreads/certbot/www -d api.whoreads.kro.kr

# 기존 인증서를 standalone → webroot로 전환할 때 (cert-name을 기존 것과 동일하게)
sudo certbot certonly --webroot -w ~/whoreads/certbot/www -d api.whoreads.kro.kr --cert-name api.whoreads.kro.kr
```

## 3. Nginx / docker-compose 설정

### 디렉토리 구조 (EC2, `~/whoreads/`)
```
~/whoreads/
├── docker-compose.yml
├── nginx/
│   ├── default.conf.template   # 배포 시 이 템플릿에서 conf.d/default.conf 생성
│   └── conf.d/
│       └── default.conf
└── certbot/
    └── www/                     # Webroot 챌린지용 (nginx 컨테이너에 마운트됨)
```

### docker-compose.yml (`whoreads-nginx` 볼륨)

```yaml
whoreads-nginx:
  volumes:
    - ./nginx/conf.d:/etc/nginx/conf.d:ro
    - ./nginx/certs:/etc/nginx/certs:ro
    - /etc/letsencrypt:/etc/letsencrypt:ro
    - ./certbot/www:/var/www/certbot:ro   # webroot 챌린지 파일을 nginx가 직접 서빙
```

이 볼륨이 빠져 있으면 nginx 설정에 `/.well-known/acme-challenge/` 라우팅이
있어도 실제 파일을 읽을 수 없어 webroot 인증이 실패합니다.

### default.conf 관련 부분 (HTTP → HTTPS + 챌린지)

```nginx
server {
    listen 80;
    server_name api.whoreads.kro.kr;

    # Let's Encrypt 인증 챌린지용
    location /.well-known/acme-challenge/ {
        root /var/www/certbot;
    }

    location / {
        return 301 https://$host$request_uri;
    }
}

server {
    listen 443 ssl;
    server_name api.whoreads.kro.kr;

    ssl_certificate /etc/letsencrypt/live/api.whoreads.kro.kr/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/api.whoreads.kro.kr/privkey.pem;
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers ECDHE-ECDSA-AES128-GCM-SHA256:ECDHE-RSA-AES128-GCM-SHA256:ECDHE-ECDSA-AES256-GCM-SHA384:ECDHE-RSA-AES256-GCM-SHA384;
    ssl_prefer_server_ciphers off;
    ssl_session_cache shared:SSL:10m;
    ssl_session_timeout 1d;

    location / {
        # Blue/Green 업스트림으로 프록시 (service-prod.inc 참고)
    }
}
```

컨테이너 볼륨을 바꾼 뒤에는 반드시 재생성해야 반영됩니다 (`docker exec ... nginx -s reload`로는 안 됨):

```bash
cd ~/whoreads
docker rm -f whoreads-nginx
docker compose up -d whoreads-nginx
```

## 4. 인증서 자동 갱신

`certbot.timer`(하루 2회)가 자동으로 `certbot renew`를 실행합니다.
webroot 모드는 nginx를 안 건드리고 인증서만 새로 받아오므로, 새 인증서를
실제로 적용하려면 **갱신 후 nginx reload가 필요**합니다. 이를 위해
deploy-hook 스크립트를 등록합니다.

```bash
sudo mkdir -p /etc/letsencrypt/renewal-hooks/deploy
sudo tee /etc/letsencrypt/renewal-hooks/deploy/reload-nginx.sh > /dev/null << 'EOF'
#!/bin/bash
docker exec whoreads-nginx nginx -s reload
EOF
sudo chmod +x /etc/letsencrypt/renewal-hooks/deploy/reload-nginx.sh
```

`/etc/letsencrypt/renewal-hooks/deploy/` 안의 스크립트는 인증서가 실제로
갱신될 때마다(호출 방식과 무관하게) certbot이 자동으로 실행해줍니다.

### 갱신 테스트

```bash
sudo certbot renew --dry-run
```

### 타이머 상태 확인

```bash
systemctl list-timers | grep certbot
sudo systemctl status certbot.timer
```

## 5. 확인

```bash
# HTTPS 접속 테스트
curl -I https://api.whoreads.kro.kr/api/health

# 인증서 정보 확인
echo | openssl s_client -connect api.whoreads.kro.kr:443 -servername api.whoreads.kro.kr 2>/dev/null | openssl x509 -noout -dates
```

## 트러블슈팅

### 인증서 발급 실패
- DNS가 EC2 IP를 가리키는지 확인: `nslookup api.whoreads.kro.kr`
- 80 포트가 열려있는지 확인: EC2 보안 그룹 확인
- 방화벽 확인: `sudo ufw status` 또는 `sudo iptables -L`

### Nginx 시작 실패
- 인증서 경로 확인: `ls -la /etc/letsencrypt/live/api.whoreads.kro.kr/`
- 설정 문법 확인: `docker exec whoreads-nginx nginx -t`

### 인증서 갱신 실패 (`Could not bind TCP port 80`)
- 갱신 방식이 `standalone`으로 잘못 설정된 경우 발생 (2번 참고). 아래로 확인:
  ```bash
  sudo cat /etc/letsencrypt/renewal/api.whoreads.kro.kr.conf | grep authenticator
  ```
  `standalone`이면 `--cert-name api.whoreads.kro.kr`로 webroot 재발급하여 전환.
- 갱신 로그 확인: `sudo cat /var/log/letsencrypt/letsencrypt.log`
- 수동 갱신 시도: `sudo certbot renew --force-renewal`

### 갱신은 성공했는데 브라우저에 여전히 만료된 인증서가 뜨는 경우
- deploy-hook이 등록되어 있는지 확인: `ls /etc/letsencrypt/renewal-hooks/deploy/`
- 수동으로 반영: `docker exec whoreads-nginx nginx -s reload`
