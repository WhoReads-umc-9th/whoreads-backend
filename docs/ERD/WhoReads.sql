CREATE TABLE `quote_source` (
	`id`	bigint	NOT NULL	DEFAULT 1,
	`quote_id`	bigint	NOT NULL	DEFAULT 1,
	`source_url`	varchar	NOT NULL,
	`source_type`	enum	NOT NULL	COMMENT 'sns, interview, video, article',
	`timestamp`	VARCHAR(255)	NULL	COMMENT '영상일 경우만 해당'
);

CREATE TABLE `quote` (
	`id`	bigint	NOT NULL	DEFAULT 1,
	`celebrity_id`	bigint	NOT NULL	DEFAULT 1,
	`bood_id`	bigint	NOT NULL	DEFAULT 1,
	`original_text`	varchar	NOT NULL,
	`confidence_score`	int	NOT NULL,
	`context_score`	int	NOT NULL,
	`language`	enum	NOT NULL	COMMENT 'ko, en',
	`book_recommendation`	boolean	NOT NULL	COMMENT 'false',
	`is_direct_quote`	boolean	NOT NULL	COMMENT '직접 인용 했는지'
);

CREATE TABLE `focus_mode` (
	`id`	bigint	NOT NULL	DEFAULT 1,
	`user_id`	bigint	NOT NULL	DEFAULT 1,
	`lock_enabled`	boolean	NULL	DEFAULT false,
	`seeting_time`	int	NULL,
	`is_white_noise_on`	boolean	NULL	DEFAULT false,
	`white_noise_type`	enum	NULL
);

CREATE TABLE `Book` (
	`id`	bigint	NOT NULL	DEFAULT 1,
	`title`	varchar	NOT NULL,
	`cover_url`	text	NOT NULL
);

CREATE TABLE `member` (
	`id`	bigint	NOT NULL	DEFAULT 1,
	`nickname`	varchar	NOT NULL,
	`gender`	enum	NOT NULL	COMMENT 'MALE, FEMALE, ETC',
	`years`	enum	NOT NULL	COMMENT 'TEENAGERS, TWENTIES, THIRTIES, ...',
	`email`	varchar	NOT NULL	COMMENT '소문자로 변환 후 DB 저장',
	`password`	varchar	NOT NULL	COMMENT '8자 이상. 공백 불가능. 해시로 저장',
	`created_at`	datetime(6)	NOT NULL,
	`updated_at`	datetime(6)	NOT NULL,
	`dna_type`	VARCHAR(255)	NULL	COMMENT 'DNS 테스트 안할 경우에는 NULL',
	`dna_type_name`	VARCHAR(255)	NOT NULL
);

CREATE TABLE `quote_context` (
	`id`	bigint	NOT NULL	DEFAULT 1,
	`quote_id`	bigint	NOT NULL	DEFAULT 1,
	`how`	varchar	NULL,
	`when`	varchar	NULL,
	`why`	varchar	NULL,
	`help`	varchar	NULL
);

CREATE TABLE `topic_quote` (
	`id`	bigint	NOT NULL	DEFAULT 1,
	`quote_id`	bigint	NOT NULL	DEFAULT 1,
	`topic_id`	bigint	NOT NULL	DEFAULT 1
);

CREATE TABLE `member_celebrity` (
	`id`	bigint	NOT NULL	DEFAULT 1,
	`member_id`	bigint	NOT NULL	DEFAULT 1,
	`celebrity_id`	bigint	NOT NULL	DEFAULT 1,
	`created_at`	datetime	NOT NULL,
	`updated_at`	datetime	NOT NULL
);

CREATE TABLE `member_book` (
	`id`	bigint	NOT NULL	DEFAULT 1,
	`member_id`	bigint	NOT NULL	DEFAULT 1,
	`book_id`	bigint	NOT NULL	DEFAULT 1,
	`status`	enum	NOT NULL	COMMENT 'wish: 담아둠, reading: 읽는 중, done: 다 읽음',
	`created_at`	datetime(6)	NOT NULL,
	`updated_at`	datetime(6)	NOT NULL
);

CREATE TABLE `alarm` (
	`id`	bigint	NOT NULL	DEFAULT 1,
	`member_id`	bigint	NOT NULL	DEFAULT 1,
	`days`	enum	NOT NULL	COMMENT '월, 화, 수,목, 금, 토, 일',
	`time`	VARCHAR(255)	NOT NULL	COMMENT '알람이 오는 시간',
	`is_enabled`	boolean	NOT NULL	DEFAULT false
);

CREATE TABLE `reading_session` (
	`id`	bigint	NOT NULL,
	`member_book_id`	bigint	NOT NULL	DEFAULT 1,
	`started_at`	datetime	NULL,
	`ended_at`	datetime	NULL,
	`duration`	int	NULL,
	`ended_by`	enum	NULL	COMMENT 'auto / manual'
);

CREATE TABLE `celebrity` (
	`id`	bigint	NOT NULL	DEFAULT 1,
	`name`	varchar(20)	NOT NULL,
	`image_url`	varchar	NULL,
	`short_bio`	varchar	NOT NULL,
	`tags`	enum	NOT NULL	COMMENT '기업가, 작가, 아티스트'
);

CREATE TABLE `topic` (
	`id`	bigint	NOT NULL	DEFAULT 1,
	`name`	enum?	NOT NULL	COMMENT '번아웃, 창업 초기, 커리어 전환',
	`description`	varchar	NOT NULL,
	`tags`	enum?	NOT NULL	COMMENT '주제 관련 키워드'
);

CREATE TABLE `Untitled2` (
	`id`	bigint	NOT NULL	DEFAULT 1
);

