# image-tools

인물 프로필 이미지를 모으고 4:5로 크롭할 때 로컬에서만 쓰는 도구. 앱 실행이나 배포와는 무관하고,
다음에 인물을 추가할 때 재사용하는 용도로 남겨둔다.

- `import_commons_selection.py` — Wikimedia Commons에서 고른 이미지를 내려받고 출처 메타데이터(CSV) 생성
- `crop_faces_4x5.py` — 얼굴 검출 기준으로 4:5 비율 일괄 크롭
- `review_crops.py` — 크롭 결과를 원본과 나란히 보여주는 육안 검수용 HTML 생성

세 스크립트 다 `--help`로 사용법을 확인할 수 있다. 배포용 스크립트(SQL, S3 업로드)는 `scripts/deploy/`에 있다.
