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
    `one_line_introduction` VARCHAR(255) NULL,
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
    `description` VARCHAR(255) NULL,
    `created_at` DATETIME(6) NOT NULL,
    `updated_at` DATETIME(6) NOT NULL,
    PRIMARY KEY (`topic_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================
-- 11. TopicQuote (주제-인용 교차 테이블)
-- =============================================
CREATE TABLE `topic_quote` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `topic_id` BIGINT NULL,
    `quote_id` BIGINT NULL,
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_topic_quote_topic` FOREIGN KEY (`topic_id`) REFERENCES `topic` (`topic_id`),
    CONSTRAINT `fk_topic_quote_quote` FOREIGN KEY (`quote_id`) REFERENCES `quote` (`quote_id`)
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
-- 15. DnaType (독서 DNA 타입) - 미구현 상태
-- =============================================
CREATE TABLE `dna_type` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `created_at` DATETIME(6) NOT NULL,
    `updated_at` DATETIME(6) NOT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================
-- 16. FocusTimerSetting (집중 타이머 설정)
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
-- 17. WhiteNoise (백색소음 마스터)
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
-- 18. BlockedApp (사용자별 차단 앱) - 미구현 가능성 있음
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
