CREATE TABLE play_review_job (
    job_key VARCHAR(80) NOT NULL PRIMARY KEY,
    version_code BIGINT NOT NULL,
    source_commit CHAR(40) NOT NULL,
    state VARCHAR(24) NOT NULL,
    last_lifecycle VARCHAR(80) NULL,
    next_check_at DATETIME(6) NOT NULL,
    lease_until DATETIME(6) NULL,
    claim_token CHAR(36) NULL,
    attempts INT NOT NULL DEFAULT 0,
    last_error VARCHAR(64) NULL,
    discord_message_id VARCHAR(32) NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    INDEX idx_play_review_due (state, next_check_at)
);
