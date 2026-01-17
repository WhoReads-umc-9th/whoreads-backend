-- 1. 로컬 DB 생성
CREATE DATABASE IF NOT EXISTS whoreads;

-- 2. 로컬 DB 및 계정 생성
CREATE USER IF NOT EXISTS 'tiger'@'%' IDENTIFIED BY 'tiger1234!';
GRANT ALL PRIVILEGES ON whoreads.* TO 'tiger'@'%';

-- 3. 변경사항 적용
FLUSH PRIVILEGES;