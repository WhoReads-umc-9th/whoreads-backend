# 수동 마이그레이션 보관함 (아카이브)

Flyway 도입(#206) 이전, SSH로 직접 운영 DB에 적용하던 수동 SQL 파일들이다.
여기 있는 3개 파일은 전부 `src/main/resources/db/migration/V1__baseline.sql`에
이미 반영된 상태(2026-10-01 운영 DB 덤프 기준)라 다시 실행할 필요 없다.

**앞으로 스키마를 바꿀 땐 이 폴더가 아니라 `src/main/resources/db/migration/`에
새 Flyway 마이그레이션 파일을 추가한다.** (README.md의 "DB 마이그레이션" 섹션 참고)
