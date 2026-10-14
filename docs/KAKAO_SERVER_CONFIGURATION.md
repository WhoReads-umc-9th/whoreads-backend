# 카카오 키 서버 관리

모바일 앱은 `GET /api/auth/kakao/authorize?state=<43~128자 URL-safe 난수>`를 WebView에 연다.
서버가 환경변수의 REST 키와 고정 콜백으로 카카오 인가 URL을 만들고 302로 연결한다.
외부에서 전달한 redirect_uri는 사용하지 않는다. 응답에는 Client Secret을 포함하지 않는다.
state 검증과 `/api/auth/kakao/callback`의 기존 회원/신규 가입 응답은 앱에서 유지한다.

실제 값은 백엔드 저장소의 GitHub Secrets `KAKAO_REST_API_KEY`, `KAKAO_CLIENT_SECRET`에서
EC2 서버의 런타임 환경변수로 전달한다. `KAKAO_REDIRECT_URI`는 배포 environment 변수이며
HTTPS `/api/auth/kakao/callback` 주소를 사용한다. 소스, Docker 이미지, 앱에는 실제 키를 쓰지 않는다.
카카오 통신 오류의 응답 본문과 예외 원문도 로그에 기록하지 않는다.

REST 키(client_id)는 OAuth 인가 URL에서 사용되는 공개 식별자이므로 브라우저 네트워크에서
볼 수 있다. 서버 관리로 앱 바이너리에 키를 고정하지 않을 수 있지만, 이 식별자를 완전히
감출 수는 없다. Client Secret과 어드민 키는 인가 URL에 전달하면 안 된다.

CI 테스트는 test 프로필의 Firebase mock을 사용하며 실제 서비스 계정 파일을 생성하지 않는다.
기존 SDK 토큰 로그인 및 콜백 API는 이전 앱 호환을 위해 유지한다.

검증: `./gradlew test --tests '*KakaoAuthorizeTest'` 및 전체 `./gradlew test`.
배포 후 비로그인 HTTPS 요청에서 302, state 보존, 고정 callback, Client Secret 미포함을 확인한다.

참고: https://developers.kakao.com/docs/ko/getting-started/security-guideline
