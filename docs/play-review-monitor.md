# Android 비공개 테스트 심사 완료 알림

모바일 저장소의 Play 제출 성공 후 전용 서버 토큰으로 `POST /api/internal/play-review/jobs`를 호출한다.
본문은 `version_code`와 제출한 40자리 `commit`이다. 앱 업로드나 새 심사 제출을 수행하는 API는 아니다.
동일 버전을 다시 등록해도 감시 시간이나 이미 보낸 알림을 초기화하지 않는다.

운영 서버의 전용 Spring 스케줄러는 등록된 작업만 처리한다. 첫 확인은 약 15초 내에 시작하며,
심사 중이면 10분 후 다시 확인한다. 작업이 없으면 Play API를 호출하지 않는다.
정확히 등록한 versionCode의 `releaseLifecycleState`가 `APPROVED_NOT_PUBLISHED` 또는 `PUBLISHED`이면
`[WhoReads] - android 비공개 테스트 심사완료`를 Discord에 전송하고 해당 감시를 종료한다.
심사 거절이나 더 최신 버전으로 대체된 작업도 종료하며 완료 알림을 보내지 않는다.

상태와 전송 기록은 MySQL `play_review_job`에 저장한다. 배포/재시작 후에도 이어서 확인하며,
DB 행 잠금과 임대 토큰으로 Blue/Green 두 인스턴스의 중복 처리를 막는다.
Discord 메시지 ID를 확인한 작업만 `SENT`로 저장한다. 전송 중 응답이 모호하거나 서버가 종료되면
`DELIVERY_UNCERTAIN`으로 보관하고 자동 재전송하지 않는다. 이 경우 서버 로그와 Discord 채널을
대조해 실제 전송 여부를 먼저 확인한다. Play 조회 오류는 10분 후 다시 확인한다.

설정은 GitHub Secrets에서 서버 컨테이너 런타임에만 주입한다.

- `PLAY_REVIEW_SERVICE_ACCOUNT_BASE64`: 기존 Play 테스트 배포 전용 서비스 계정 JSON의 Base64
- `DISCORD_WEBHOOK_URL`: 사용자가 지정한 알림 채널
- `PLAY_REVIEW_MONITOR_TOKEN`: 모바일 CI와 서버가 공유하는 등록 전용 토큰
- `PLAY_REVIEW_ENABLED=true`: 운영 환경에서만 활성화

모든 내부 API는 `X-Play-Review-Token` 헤더가 필요하다.
`GET /api/internal/play-review/health`로 실제 Play/Discord 연결과 남은 작업 수를 확인하고,
`GET /api/internal/play-review/jobs/release-{versionCode}`로 특정 감시 상태를 확인한다.
기존 알림 이관 시에는 등록 본문에 `prior_message_id`를 넣어 이미 보낸 알림을 보존할 수 있다.

`POST /api/internal/play-review/probes`는 동일한 버전/커밋 본문을 받아 실제 서버 스케줄러와
Play API를 거쳐 명확히 ‘알림 연결 테스트’라고 표시한 메시지를 보낸다.
운영 버전 감시나 그 전송 기록을 변경하지 않는다. 모바일 저장소의
`Verify Backend Play Review Scheduler` 수동 워크플로가 이 검증과 이관을 수행한다.

공식 API: https://developers.google.com/android-publisher/api-ref/rest/v3/applications.tracks.releases/list
