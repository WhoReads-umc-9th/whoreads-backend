-- =============================================================
-- 인물 이미지 라이선스 버전 컬럼 추가 (이슈 #207)
--
-- CC BY/BY-SA는 버전(2.0/3.0/4.0)마다 별개의 법적 문서다.
-- 기존 image_license 컬럼은 계열(CC_BY, CC_BY_SA)만 저장해 버전 정보가
-- 소실되고 있었다. 저작자 표시 문구에는 버전까지 함께 노출해야 한다.
--
-- ⚠️ 배포 순서: 이 SQL을 운영 DB에 먼저 적용한 뒤 애플리케이션을 배포할 것.
--    application-prod.yml이 ddl-auto: validate 이므로, 컬럼 없이 배포하면
--    Hibernate 스키마 검증에 실패해 서버가 기동되지 않는다.
-- =============================================================

ALTER TABLE `celebrity`
    ADD COLUMN `image_license_version` VARCHAR(10) NULL
        COMMENT '라이선스 버전 (예: 2.0, 3.0, 4.0). 유튜브 CC BY처럼 버전 개념이 없는 출처는 NULL';

-- 롤백
-- ALTER TABLE `celebrity` DROP COLUMN `image_license_version`;
