ALTER TABLE diaries ADD COLUMN emotion_score INT NULL;
ALTER TABLE diaries ADD COLUMN body_score INT NULL;
ALTER TABLE diaries ADD COLUMN emotion_tags VARCHAR(500) NULL;
ALTER TABLE diaries ADD COLUMN body_tags VARCHAR(500) NULL;
ALTER TABLE diaries DROP COLUMN mood;
ALTER TABLE diaries DROP COLUMN stress;
ALTER TABLE diaries DROP COLUMN fatigue;

ALTER TABLE activities ADD COLUMN steps INT NOT NULL DEFAULT 0;
ALTER TABLE activities ADD COLUMN exercise_minutes INT NOT NULL DEFAULT 0;
ALTER TABLE activities ADD COLUMN burned_kcal INT NOT NULL DEFAULT 0;
UPDATE activities SET burned_kcal = activity_amount;
ALTER TABLE activities DROP COLUMN activity_amount;

CREATE TABLE body_checks (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    recorded_date DATE NOT NULL,
    original_image_url VARCHAR(1000) NOT NULL,
    expected_image_url VARCHAR(1000) NULL,
    analysis_status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_body_checks_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX idx_body_checks_user_date ON body_checks(user_id, recorded_date);
