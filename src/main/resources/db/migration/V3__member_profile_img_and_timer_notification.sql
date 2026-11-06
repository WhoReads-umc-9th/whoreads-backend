-- 스테이징 등 일부 DB에는 member.profile_img_id가 이미 있다 (Flyway 도입 전 수동/ddl-auto로 생성).
-- 컬럼이 없을 때만 추가하고, 있으면 NULL 값을 기본 이미지(0)로 채워 NOT NULL로 맞춘다. 몇 번 실행해도 결과가 같다.
SET @has_profile_img_id = (
  SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'member' AND COLUMN_NAME = 'profile_img_id'
);
SET @add_profile_img_id = IF(@has_profile_img_id = 0,
  'ALTER TABLE `member` ADD COLUMN `profile_img_id` int NOT NULL DEFAULT 0',
  'SELECT 1');
PREPARE stmt FROM @add_profile_img_id;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

UPDATE `member` SET `profile_img_id` = 0 WHERE `profile_img_id` IS NULL;
ALTER TABLE `member` MODIFY COLUMN `profile_img_id` int NOT NULL DEFAULT 0;

-- 타이머 완료 알림(COMPLETED_TIMER)을 알림 이력에 저장할 수 있도록 enum 확장 (이미 확장돼 있어도 같은 결과)
ALTER TABLE `notification_history`
  MODIFY COLUMN `type` enum('FOLLOW','ROUTINE','COMPLETED_TIMER') NOT NULL;
