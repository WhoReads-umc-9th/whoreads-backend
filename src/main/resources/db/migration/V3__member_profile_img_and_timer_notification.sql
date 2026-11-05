-- 사용자가 선택한 프로필 이미지 번호 (0~5). 기존 회원은 기본 이미지(0)로 채운다.
ALTER TABLE `member`
  ADD COLUMN `profile_img_id` int NOT NULL DEFAULT 0;

-- 타이머 완료 알림(COMPLETED_TIMER)을 알림 이력에 저장할 수 있도록 enum 확장
ALTER TABLE `notification_history`
  MODIFY COLUMN `type` enum('FOLLOW','ROUTINE','COMPLETED_TIMER') NOT NULL;
