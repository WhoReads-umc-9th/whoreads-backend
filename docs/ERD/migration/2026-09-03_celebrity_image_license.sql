-- =============================================================
-- 인물 이미지 저작권/출처 컬럼 추가 (이슈 #205)
--
-- ⚠️ 배포 순서: 이 SQL을 운영 DB에 먼저 적용한 뒤 애플리케이션을 배포할 것.
--    application-prod.yml이 ddl-auto: validate 이므로, 컬럼 없이 배포하면
--    Hibernate 스키마 검증에 실패해 서버가 기동되지 않는다.
--
-- 대상: prod, staging (staging은 ddl-auto: update라 자동 반영되지만 명시적으로 실행 권장)
-- 롤백: 파일 하단 참조
-- =============================================================

ALTER TABLE `celebrity`
    ADD COLUMN `image_source_url` TEXT NULL COMMENT '이미지를 가져온 원본 페이지 URL',
    ADD COLUMN `image_author`     VARCHAR(100) NULL COMMENT '사진 저작자 표기명',
    ADD COLUMN `image_license`    VARCHAR(30) NULL COMMENT 'PUBLIC_DOMAIN, CC0, CC_BY, CC_BY_SA, KOGL_TYPE1, OWNED, UNKNOWN';

-- 기존 120건은 출처가 확인되지 않은 상태이므로 UNKNOWN으로 표시한다.
-- (엔티티에서 NULL도 UNKNOWN과 동일하게 '출처 표기 필요'로 취급하지만,
--  "아직 정리 안 된 이미지"를 쿼리로 집계할 수 있도록 명시적으로 채워둔다)
UPDATE `celebrity`
SET `image_license` = 'UNKNOWN'
WHERE `image_license` IS NULL
  AND `image_url` IS NOT NULL;

-- 정리 진행률 확인용
-- SELECT image_license, COUNT(*) FROM celebrity GROUP BY image_license;


-- =============================================================
-- 롤백
-- =============================================================
-- ALTER TABLE `celebrity`
--     DROP COLUMN `image_source_url`,
--     DROP COLUMN `image_author`,
--     DROP COLUMN `image_license`;
