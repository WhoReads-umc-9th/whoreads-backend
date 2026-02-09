-- WhoReads Database DDL
-- Generated from JPA Entities (2026-02-08)

-- =============================================
-- 1. Member (회원)
-- =============================================
CREATE TABLE `member` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `nickname` VARCHAR(255) NOT NULL,
    `gender` ENUM('MALE', 'FEMALE', 'ETC') NOT NULL,
    `age_group` ENUM('TEENAGERS', 'TWENTIES', 'THIRTIES', 'FORTIES', 'FIFTY_PLUS') NOT NULL,
    `email` VARCHAR(255) NOT NULL UNIQUE,
    `login_id` VARCHAR(255) NOT NULL UNIQUE,
    `password` VARCHAR(255) NOT NULL,
    `status` ENUM('INACTIVE', 'ACTIVE') NULL,
    `dna_type` VARCHAR(255) NULL,
    `dna_type_name` VARCHAR(255) NULL,
    `fcm_token` VARCHAR(255) NULL,
    `fcm_token_updated_at` DATETIME(6) NULL,
    `deleted_at` DATETIME(6) NULL,
    `created_at` DATETIME(6) NOT NULL,
    `updated_at` DATETIME(6) NOT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================
-- 2. Book (책)
-- =============================================
CREATE TABLE `book` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `title` VARCHAR(255) NOT NULL,
    `author_name` VARCHAR(255) NOT NULL,
    `link` TEXT NULL,
    `genre` VARCHAR(255) NULL,
    `cover_url` TEXT NULL,
    `total_page` INT NULL,
    `created_at` DATETIME(6) NOT NULL,
    `updated_at` DATETIME(6) NOT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================
-- 3. UserBook (사용자 서재)
-- =============================================
CREATE TABLE `user_book` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `member_id` BIGINT NOT NULL,
    `book_id` BIGINT NOT NULL,
    `reading_status` ENUM('WISH', 'READING', 'COMPLETE') NOT NULL,
    `reading_page` INT NULL,
    `created_at` DATETIME(6) NOT NULL,
    `updated_at` DATETIME(6) NOT NULL,
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_user_book_member` FOREIGN KEY (`member_id`) REFERENCES `member` (`id`),
    CONSTRAINT `fk_user_book_book` FOREIGN KEY (`book_id`) REFERENCES `book` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================
-- 4. Celebrity (유명인)
-- =============================================
CREATE TABLE `celebrity` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(50) NOT NULL,
    `image_url` TEXT NULL,
    `short_bio` VARCHAR(255) NOT NULL,
    `created_at` DATETIME(6) NOT NULL,
    `updated_at` DATETIME(6) NOT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================
-- 5. CelebrityJobTags (유명인 직업 태그 - ElementCollection)
-- =============================================
CREATE TABLE `celebrity_job_tags` (
    `celebrity_id` BIGINT NOT NULL,
    `job_tag` ENUM('ENTREPRENEUR', 'WRITER', 'ATHLETE', 'MOVIE_DIRECTOR', 'SINGER',
                   'CHEF', 'ACTOR', 'MUSICAL_ACTOR', 'INSTRUCTOR', 'SCHOLAR',
                   'PROFILER', 'LITERARY_CRITIC', 'IDOL', 'SCIENCE_MUSEUM_DIRECTOR',
                   'YOUTUBER', 'MEDIA_CRITIC', 'ANNOUNCER', 'TRANSLATOR', 'COMEDIAN',
                   'LYRICIST', 'FILM_CRITIC', 'BIOLOGIST', 'PRESIDENT', 'POLITICIAN', 'PROFESSOR') NOT NULL,
    CONSTRAINT `fk_celebrity_job_tags_celebrity` FOREIGN KEY (`celebrity_id`) REFERENCES `celebrity` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================
-- 6. Quote (인용)
-- =============================================
CREATE TABLE `quote` (
    `quote_id` BIGINT NOT NULL AUTO_INCREMENT,
    `celebrity_id` BIGINT NULL,
    `original_text` TEXT NOT NULL,
    `language` ENUM('KO', 'EN') NULL,
    `context_score` INT NOT NULL DEFAULT 0,
    `created_at` DATETIME(6) NOT NULL,
    `updated_at` DATETIME(6) NOT NULL,
    PRIMARY KEY (`quote_id`),
    CONSTRAINT `fk_quote_celebrity` FOREIGN KEY (`celebrity_id`) REFERENCES `celebrity` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================
-- 7. BookQuote (책-인용 교차 테이블)
-- =============================================
CREATE TABLE `book_quote` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `book_id` BIGINT NULL,
    `quote_id` BIGINT NULL,
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_book_quote_book` FOREIGN KEY (`book_id`) REFERENCES `book` (`id`),
    CONSTRAINT `fk_book_quote_quote` FOREIGN KEY (`quote_id`) REFERENCES `quote` (`quote_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================
-- 8. QuoteContext (인용 맥락)
-- =============================================
CREATE TABLE `quote_context` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `quote_id` BIGINT NULL,
    `context_how` VARCHAR(255) NULL COMMENT '읽게 된 계기',
    `context_when` VARCHAR(255) NULL COMMENT '어떤 시기에 읽었는지',
    `context_why` VARCHAR(255) NULL COMMENT '왜 이 책이었나',
    `context_help` VARCHAR(255) NULL COMMENT '어떤 도움을 받았나',
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_quote_context_quote` FOREIGN KEY (`quote_id`) REFERENCES `quote` (`quote_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================
-- 9. QuoteSource (인용 출처)
-- =============================================
CREATE TABLE `quote_source` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `quote_id` BIGINT NULL,
    `source_url` VARCHAR(255) NULL,
    `source_type` ENUM('INTERVIEW', 'VIDEO', 'SOCIAL_MEDIA', 'ARTICLE', 'MAGAZINE', 'ETC') NULL,
    `timestamp` VARCHAR(255) NULL COMMENT '영상일 경우 타임스탬프',
    `is_direct_quote` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '직접 인용 여부',
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_quote_source_quote` FOREIGN KEY (`quote_id`) REFERENCES `quote` (`quote_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================
-- 10. Topic (주제)
-- =============================================
CREATE TABLE `topic` (
    `topic_id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(255) NOT NULL UNIQUE,
    `created_at` DATETIME(6) NOT NULL,
    `updated_at` DATETIME(6) NOT NULL,
    PRIMARY KEY (`topic_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================
-- 11. TopicBook (주제-책 교차 테이블)
-- =============================================
CREATE TABLE `topic_book` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `book_id` BIGINT NULL,
    `topic_id` BIGINT NULL,
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_topic_book_book` FOREIGN KEY (`book_id`) REFERENCES `book` (`id`),
    CONSTRAINT `fk_topic_book_topic` FOREIGN KEY (`topic_id`) REFERENCES `topic` (`topic_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================
-- 12. Notification (알림)
-- =============================================
CREATE TABLE `notification` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `member_id` BIGINT NOT NULL,
    `type` ENUM('FOLLOW', 'ROUTINE') NOT NULL,
    `days` JSON NULL COMMENT '요일 목록 (MONDAY, TUESDAY, ...)',
    `time` TIME NULL,
    `is_enabled` TINYINT(1) NOT NULL DEFAULT 1,
    `created_at` DATETIME(6) NOT NULL,
    `updated_at` DATETIME(6) NOT NULL,
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_notification_member` FOREIGN KEY (`member_id`) REFERENCES `member` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================
-- 13. ReadingSession (독서 세션)
-- =============================================
CREATE TABLE `reading_session` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `member_id` BIGINT NOT NULL,
    `status` ENUM('IN_PROGRESS', 'PAUSED', 'COMPLETED') NOT NULL,
    `total_minutes` BIGINT NULL,
    `finished_at` DATETIME(6) NULL,
    `created_at` DATETIME(6) NOT NULL,
    `updated_at` DATETIME(6) NOT NULL,
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_reading_session_member` FOREIGN KEY (`member_id`) REFERENCES `member` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================
-- 14. ReadingInterval (독서 인터벌)
-- =============================================
CREATE TABLE `reading_interval` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `session_id` BIGINT NOT NULL,
    `start_time` DATETIME(6) NOT NULL,
    `end_time` DATETIME(6) NULL,
    `duration_minutes` BIGINT NULL,
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_reading_interval_session` FOREIGN KEY (`session_id`) REFERENCES `reading_session` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================
-- 15. DnaTrack (DNA 테스트 트랙)
-- =============================================
CREATE TABLE `dna_track` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `track_code` ENUM('COMFORT', 'HABIT', 'CAREER', 'INSIGHT', 'FOCUS') NOT NULL COMMENT '마음정리, 습관, 커리어, 사고확장, 몰입',
    `name` VARCHAR(255) NOT NULL COMMENT '사용자용 이름',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================
-- 16. DnaQuestion (DNA 테스트 질문)
-- =============================================
CREATE TABLE `dna_question` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `track_id` BIGINT NULL,
    `step` INT NOT NULL COMMENT '질문 순서 (1~5)',
    `content` VARCHAR(255) NOT NULL COMMENT '질문 내용',
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_dna_question_track` FOREIGN KEY (`track_id`) REFERENCES `dna_track` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================
-- 17. DnaOption (DNA 테스트 선택지)
-- =============================================
CREATE TABLE `dna_option` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `question_id` BIGINT NULL,
    `track_id` BIGINT NULL,
    `content` VARCHAR(255) NOT NULL COMMENT '선택지 내용',
    `genre` ENUM('LITERATURE', 'ESSAY', 'HUMANITIES', 'SOCIETY', 'SCIENCE', 'ECONOMY', 'PSYCHOLOGY') NULL COMMENT '장르',
    `score` INT NOT NULL COMMENT '선택 시 장르 점수',
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_dna_option_question` FOREIGN KEY (`question_id`) REFERENCES `dna_question` (`id`),
    CONSTRAINT `fk_dna_option_track` FOREIGN KEY (`track_id`) REFERENCES `dna_track` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================
-- 18. DnaResult (DNA 테스트 결과)
-- =============================================
CREATE TABLE `dna_result` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `member_id` BIGINT NULL,
    `track_id` BIGINT NULL,
    `celebrity_id` BIGINT NULL,
    `created_at` DATETIME(6) NOT NULL,
    `updated_at` DATETIME(6) NOT NULL,
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_dna_result_member` FOREIGN KEY (`member_id`) REFERENCES `member` (`id`),
    CONSTRAINT `fk_dna_result_track` FOREIGN KEY (`track_id`) REFERENCES `dna_track` (`id`),
    CONSTRAINT `fk_dna_result_celebrity` FOREIGN KEY (`celebrity_id`) REFERENCES `celebrity` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================
-- 19. FocusTimerSetting (집중 타이머 설정)
-- =============================================
CREATE TABLE `focus_timer_setting` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `member_id` BIGINT NOT NULL UNIQUE,
    `focus_block_enabled` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '집중 차단 활성화',
    `white_noise_enabled` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '백색소음 활성화',
    `created_at` DATETIME(6) NOT NULL,
    `updated_at` DATETIME(6) NOT NULL,
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_focus_timer_setting_member` FOREIGN KEY (`member_id`) REFERENCES `member` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================
-- 20. WhiteNoise (백색소음 마스터)
-- =============================================
CREATE TABLE `white_noise` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(50) NOT NULL,
    `audio_url` TEXT NOT NULL,
    `created_at` DATETIME(6) NOT NULL,
    `updated_at` DATETIME(6) NOT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================
-- 21. BlockedApp (사용자별 차단 앱) - 미구현 가능성 있음
-- =============================================
CREATE TABLE `blocked_app` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `member_id` BIGINT NOT NULL,
    `bundle_id` VARCHAR(255) NOT NULL,
    `name` VARCHAR(100) NOT NULL,
    `created_at` DATETIME(6) NOT NULL,
    `updated_at` DATETIME(6) NOT NULL,
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_blocked_app_member` FOREIGN KEY (`member_id`) REFERENCES `member` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================
-- Indexes (성능 최적화용)
-- =============================================
CREATE INDEX `idx_user_book_member` ON `user_book` (`member_id`);
CREATE INDEX `idx_user_book_book` ON `user_book` (`book_id`);
CREATE INDEX `idx_quote_celebrity` ON `quote` (`celebrity_id`);
CREATE INDEX `idx_notification_member` ON `notification` (`member_id`);
CREATE INDEX `idx_reading_session_member` ON `reading_session` (`member_id`);
CREATE INDEX `idx_reading_interval_session` ON `reading_interval` (`session_id`);
CREATE INDEX `idx_blocked_app_member` ON `blocked_app` (`member_id`);
CREATE INDEX `idx_dna_question_track` ON `dna_question` (`track_id`);
CREATE INDEX `idx_dna_option_question` ON `dna_option` (`question_id`);
CREATE INDEX `idx_dna_result_member` ON `dna_result` (`member_id`);

-- =============================================
-- Triggers (context_score 자동 계산)
-- =============================================
-- quote_context INSERT 시 quote.context_score 자동 업데이트
-- how: +1, when: +1, why: +2, help: +1
DELIMITER //
CREATE TRIGGER `trg_quote_context_insert` AFTER INSERT ON `quote_context`
FOR EACH ROW
BEGIN
    DECLARE score INT DEFAULT 0;
    IF NEW.context_how IS NOT NULL AND NEW.context_how != '' THEN SET score = score + 1; END IF;
    IF NEW.context_when IS NOT NULL AND NEW.context_when != '' THEN SET score = score + 1; END IF;
    IF NEW.context_why IS NOT NULL AND NEW.context_why != '' THEN SET score = score + 2; END IF;
    IF NEW.context_help IS NOT NULL AND NEW.context_help != '' THEN SET score = score + 1; END IF;
    UPDATE `quote` SET `context_score` = score WHERE `quote_id` = NEW.quote_id;
END //

CREATE TRIGGER `trg_quote_context_update` AFTER UPDATE ON `quote_context`
FOR EACH ROW
BEGIN
    DECLARE score INT DEFAULT 0;
    IF NEW.context_how IS NOT NULL AND NEW.context_how != '' THEN SET score = score + 1; END IF;
    IF NEW.context_when IS NOT NULL AND NEW.context_when != '' THEN SET score = score + 1; END IF;
    IF NEW.context_why IS NOT NULL AND NEW.context_why != '' THEN SET score = score + 2; END IF;
    IF NEW.context_help IS NOT NULL AND NEW.context_help != '' THEN SET score = score + 1; END IF;
    UPDATE `quote` SET `context_score` = score WHERE `quote_id` = NEW.quote_id;
END //
DELIMITER ;
