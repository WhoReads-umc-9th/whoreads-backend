-- =============================================================
-- Flyway 베이스라인 (이슈 #206)
--
-- 2026-10-01 시점 운영 DB(whoreads)의 실제 스키마를 mysqldump --no-data로
-- 그대로 떠서 만들었다. 엔티티나 docs/ERD/WhoReads.sql 기준이 아니다 —
-- 둘 다 실제 운영 스키마와 어긋나 있었기 때문(#206, #217 참고).
--
-- quote_context_bak_20260831, quote_source_bak_20260831 백업 테이블은
-- 코드 어디서도 참조하지 않는 일회성 잔재라 베이스라인에서 제외했고,
-- 운영 DB에서도 함께 DROP했다.
--
-- Flyway 설정(spring.flyway.baseline-on-migrate=true, baseline-version=1)
-- 하에서는 flyway_schema_history에 이미 V1이 적용된 것으로 기록만 되고
-- 이 파일 자체는 기존 DB(prod/staging)에 실행되지 않는다. 로컬처럼
-- 스키마가 비어있는 환경에서만 실제로 실행되어 전체 스키마를 만든다.
-- =============================================================

SET FOREIGN_KEY_CHECKS = 0;

CREATE TABLE `blocked_app` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `bundle_id` varchar(255) NOT NULL,
  `name` varchar(100) NOT NULL,
  `member_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKabtj6se5bnns2wg442f7dvt13` (`member_id`),
  CONSTRAINT `FKabtj6se5bnns2wg442f7dvt13` FOREIGN KEY (`member_id`) REFERENCES `member` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `book` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` datetime(6) DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  `author_name` varchar(255) NOT NULL,
  `cover_url` text,
  `genre` varchar(255) DEFAULT NULL,
  `link` text,
  `title` varchar(255) NOT NULL,
  `total_page` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKm7f4lnykknjmq4rab0tlwyy9l` (`title`,`author_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `celebrity` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` datetime(6) DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  `image_url` text,
  `name` varchar(50) NOT NULL,
  `result_comment` text,
  `short_bio` varchar(255) NOT NULL,
  `image_author` varchar(100) DEFAULT NULL,
  `image_license` enum('CC0','CC_BY','CC_BY_SA','KOGL_TYPE1','PUBLIC_DOMAIN','UNKNOWN') DEFAULT NULL,
  `image_license_version` varchar(10) DEFAULT NULL,
  `image_source_url` text,
  `is_edited` bit(1) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `book_quote` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `book_id` bigint NOT NULL,
  `quote_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKl720846fgsjo55igmdko9gocs` (`book_id`,`quote_id`),
  KEY `FK51k2ehj8fyurb35pq7ivdejd8` (`quote_id`),
  CONSTRAINT `FK2n72tpci61jwnmda2d3nk8cb2` FOREIGN KEY (`book_id`) REFERENCES `book` (`id`),
  CONSTRAINT `FK51k2ehj8fyurb35pq7ivdejd8` FOREIGN KEY (`quote_id`) REFERENCES `quote` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `celebrity_book` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `book_id` bigint DEFAULT NULL,
  `celebrity_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKeah6aqn400j5ou2ocpbx4tqyh` (`book_id`),
  KEY `FKj4n9l0vxf1kqr14t6r9m7iiuo` (`celebrity_id`),
  CONSTRAINT `FKeah6aqn400j5ou2ocpbx4tqyh` FOREIGN KEY (`book_id`) REFERENCES `book` (`id`),
  CONSTRAINT `FKj4n9l0vxf1kqr14t6r9m7iiuo` FOREIGN KEY (`celebrity_id`) REFERENCES `celebrity` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `celebrity_job_tags` (
  `celebrity_id` bigint NOT NULL,
  `job_tag` enum('ACTOR','ANNOUNCER','ATHLETE','BIOLOGIST','CHEF','COACH','COMEDIAN','ENTREPRENEUR','FILM_CRITIC','IDOL','INSTRUCTOR','JOURNALIST','LAWYER','LITERARY_CRITIC','LYRICIST','MEDIA_CRITIC','MOVIE_DIRECTOR','MUSICAL_ACTOR','NOVELIST','PHYSICIST','POLITICIAN','PRESIDENT','PROFESSOR','PROFILER','RAPPER','SCHOLAR','SINGER','SPORTS_COMMENTATOR','TRANSLATOR','WRITER','YOUTUBER') DEFAULT NULL,
  KEY `FKph09rv6auld2odi5rsvm1bwol` (`celebrity_id`),
  CONSTRAINT `FKph09rv6auld2odi5rsvm1bwol` FOREIGN KEY (`celebrity_id`) REFERENCES `celebrity` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `dna_track` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `track_code` enum('CAREER','COMFORT','FOCUS','HABIT','INSIGHT') NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `dna_option` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `content` varchar(255) NOT NULL,
  `genre` enum('ECONOMY','ESSAY','HUMANITIES','LITERATURE','PSYCHOLOGY','SCIENCE','SOCIETY') DEFAULT NULL,
  `score` int NOT NULL,
  `question_id` bigint DEFAULT NULL,
  `track_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK7okjf9e178ey10lq23oxfv3ow` (`question_id`),
  KEY `FKo49dym1foh9xdaq5oynmgjht8` (`track_id`),
  CONSTRAINT `FK7okjf9e178ey10lq23oxfv3ow` FOREIGN KEY (`question_id`) REFERENCES `dna_question` (`id`),
  CONSTRAINT `FKo49dym1foh9xdaq5oynmgjht8` FOREIGN KEY (`track_id`) REFERENCES `dna_track` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `dna_question` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `content` varchar(255) NOT NULL,
  `step` int NOT NULL,
  `track_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKcjfnpcuoebfsosiqe5hokocs1` (`track_id`),
  CONSTRAINT `FKcjfnpcuoebfsosiqe5hokocs1` FOREIGN KEY (`track_id`) REFERENCES `dna_track` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `member` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `age_group` enum('FIFTY_PLUS','FORTIES','TEENAGERS','THIRTIES','TWENTIES') NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `dna_type` varchar(255) DEFAULT NULL,
  `dna_type_name` varchar(255) DEFAULT NULL,
  `email` varchar(255) NOT NULL,
  `fcm_token` varchar(255) DEFAULT NULL,
  `fcm_token_updated_at` datetime(6) DEFAULT NULL,
  `gender` enum('ETC','FEMALE','MALE') NOT NULL,
  `login_id` varchar(255) NOT NULL,
  `nickname` varchar(255) NOT NULL,
  `password` varchar(255) NOT NULL,
  `status` enum('ACTIVE','INACTIVE') NOT NULL,
  `provider` enum('KAKAO','LOCAL') NOT NULL,
  `provider_id` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKmbmcqelty0fbrvxp1q58dn57t` (`email`),
  UNIQUE KEY `UKenfm5patwjqulw8k4wwuo6f60` (`login_id`),
  UNIQUE KEY `UKgyl5t9mxo4q408lurfmndln0b` (`provider`,`provider_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `dna_result` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `celebrity_id` bigint DEFAULT NULL,
  `member_id` bigint DEFAULT NULL,
  `track_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK8cd0oye4at1k7pkot6vikn1mo` (`celebrity_id`),
  KEY `FK87osgp6jkknkibyglmu5v056m` (`member_id`),
  KEY `FK2m8omkw97o4qhdixr417uror8` (`track_id`),
  CONSTRAINT `FK2m8omkw97o4qhdixr417uror8` FOREIGN KEY (`track_id`) REFERENCES `dna_track` (`id`),
  CONSTRAINT `FK87osgp6jkknkibyglmu5v056m` FOREIGN KEY (`member_id`) REFERENCES `member` (`id`),
  CONSTRAINT `FK8cd0oye4at1k7pkot6vikn1mo` FOREIGN KEY (`celebrity_id`) REFERENCES `celebrity` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `focus_timer_setting` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `focus_block_enabled` bit(1) NOT NULL,
  `white_noise_enabled` bit(1) NOT NULL,
  `member_id` bigint NOT NULL,
  `timer_minutes` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKoilv6c1pvtilupp5cwv71i8tl` (`member_id`),
  CONSTRAINT `FK8jfcwyxr9dmbxq16s0650y2ja` FOREIGN KEY (`member_id`) REFERENCES `member` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `member_celebrity` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `celebrity_id` bigint DEFAULT NULL,
  `member_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKf5xosqevl6n7i9g9ewpvcua6y` (`member_id`,`celebrity_id`),
  KEY `FK649dfca8n4fqspebysw69wmol` (`celebrity_id`),
  CONSTRAINT `FK3c0fqxhsswdc2gk70dn5ajv1o` FOREIGN KEY (`member_id`) REFERENCES `member` (`id`),
  CONSTRAINT `FK649dfca8n4fqspebysw69wmol` FOREIGN KEY (`celebrity_id`) REFERENCES `celebrity` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `notification_history` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `body` varchar(255) NOT NULL,
  `link` varchar(255) DEFAULT NULL,
  `title` varchar(255) NOT NULL,
  `type` enum('FOLLOW','ROUTINE') NOT NULL,
  `member_id` bigint NOT NULL,
  `is_read` bit(1) NOT NULL,
  `book_id` bigint DEFAULT NULL,
  `celebrity_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK9akr1h83p010kjcs3p36dsgxd` (`member_id`),
  CONSTRAINT `FK9akr1h83p010kjcs3p36dsgxd` FOREIGN KEY (`member_id`) REFERENCES `member` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `notification_setting` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `days` json DEFAULT NULL,
  `is_enabled` bit(1) NOT NULL,
  `time` time DEFAULT NULL,
  `type` enum('FOLLOW','ROUTINE') NOT NULL,
  `member_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKlfw5h9l15kryakerd2pw23mkp` (`member_id`),
  CONSTRAINT `FKlfw5h9l15kryakerd2pw23mkp` FOREIGN KEY (`member_id`) REFERENCES `member` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `quote` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` datetime(6) DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  `context_score` int NOT NULL,
  `language` enum('EN','KO') NOT NULL,
  `original_text` text NOT NULL,
  `celebrity_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKiuk5h9adew6vd0iubhosibvad` (`celebrity_id`),
  CONSTRAINT `FKiuk5h9adew6vd0iubhosibvad` FOREIGN KEY (`celebrity_id`) REFERENCES `celebrity` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `quote_context` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `context_help` text,
  `context_how` text,
  `context_when` text,
  `context_why` text,
  `quote_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKpubrk9f7vyxt9f95wf9oyow20` (`quote_id`),
  UNIQUE KEY `uk_quote_context_quote` (`quote_id`),
  CONSTRAINT `FKmn91egeelnr1mbcue80papjpn` FOREIGN KEY (`quote_id`) REFERENCES `quote` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `quote_source` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `source_type` enum('ARTICLE','INTERVIEW','MAGAZINE','SNS','YOUTUBE_VIDEO') NOT NULL,
  `source_url` text,
  `timestamp` varchar(255) DEFAULT NULL,
  `quote_id` bigint NOT NULL,
  `is_direct_quote` bit(1) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK8gvw0i2n2v52g6exvbma26ma4` (`quote_id`),
  UNIQUE KEY `uk_quote_source_quote` (`quote_id`),
  CONSTRAINT `FKjoc2esqnx4sb6tk3wjv3oaylh` FOREIGN KEY (`quote_id`) REFERENCES `quote` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `reading_session` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `finished_at` datetime(6) DEFAULT NULL,
  `status` varchar(50) NOT NULL,
  `total_minutes` bigint DEFAULT NULL,
  `member_id` bigint NOT NULL,
  `last_heartbeat_at` datetime(6) DEFAULT NULL,
  `remaining_minutes` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKfb7kcbrwae3q7eri5qyq3gqep` (`member_id`),
  CONSTRAINT `FKfb7kcbrwae3q7eri5qyq3gqep` FOREIGN KEY (`member_id`) REFERENCES `member` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `reading_interval` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `duration_minutes` bigint DEFAULT NULL,
  `end_time` datetime(6) DEFAULT NULL,
  `start_time` datetime(6) NOT NULL,
  `session_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKl1op7joel27wfb733m4uctchl` (`session_id`),
  CONSTRAINT `FKl1op7joel27wfb733m4uctchl` FOREIGN KEY (`session_id`) REFERENCES `reading_session` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `topic` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` enum('HUMAN_UNDERSTANDING','LIFE_DIRECTION','MINDSET','SOCIETY','TOP_20','TURNING_POINT') NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKmbunn9erv8nmf5lk1r2nu0nex` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `topic_book` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `book_id` bigint NOT NULL,
  `topic_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKfk1x6hrtcn5rifaxvm1145ajr` (`book_id`),
  KEY `FK1o2p9ouikj5q45h1788hlwq3s` (`topic_id`),
  CONSTRAINT `FK1o2p9ouikj5q45h1788hlwq3s` FOREIGN KEY (`topic_id`) REFERENCES `topic` (`id`),
  CONSTRAINT `FKfk1x6hrtcn5rifaxvm1145ajr` FOREIGN KEY (`book_id`) REFERENCES `book` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `user_book` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `completed_at` date DEFAULT NULL,
  `reading_page` int DEFAULT NULL,
  `reading_status` enum('COMPLETE','READING','WISH') NOT NULL,
  `started_at` date DEFAULT NULL,
  `book_id` bigint NOT NULL,
  `member_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK85pwltn867pjxog1gk5smtqcw` (`book_id`),
  KEY `FKl5rf7f196ogr452bcb2493cq5` (`member_id`),
  CONSTRAINT `FK85pwltn867pjxog1gk5smtqcw` FOREIGN KEY (`book_id`) REFERENCES `book` (`id`),
  CONSTRAINT `FKl5rf7f196ogr452bcb2493cq5` FOREIGN KEY (`member_id`) REFERENCES `member` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `white_noise` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `audio_url` text NOT NULL,
  `name` varchar(50) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

SET FOREIGN_KEY_CHECKS = 1;
