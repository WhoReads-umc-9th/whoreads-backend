-- 독서 세션별 타이머 주기 (세션 시작 시 프론트에서 전달, 분 단위)
-- 기존 세션은 이전 서버 동작(heartbeat 1분, 5분 무응답 시 중단, 2시간 경과 시 완료)과 같은 값으로 채운다.
ALTER TABLE `reading_session`
  ADD COLUMN `heartbeat_interval_minutes` bigint NOT NULL DEFAULT 1,
  ADD COLUMN `suspend_after_minutes` bigint NOT NULL DEFAULT 5,
  ADD COLUMN `complete_after_minutes` bigint NOT NULL DEFAULT 120;
