CREATE TABLE diaries (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    record_date DATE NOT NULL,
    memo VARCHAR(1000) NULL,
    condition_score DECIMAL(2,1) NULL,
    weight_kg DECIMAL(5,1) NULL,
    water_ml INT NULL,
    skin_condition VARCHAR(30) NULL,
    menstrual_status BOOLEAN NULL,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_diaries_user_date UNIQUE (user_id, record_date),
    CONSTRAINT fk_diaries_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE meals (
    id BIGINT NOT NULL AUTO_INCREMENT,
    diary_id BIGINT NOT NULL,
    meal_type VARCHAR(20) NOT NULL,
    food_name VARCHAR(100) NOT NULL,
    calories INT NOT NULL,
    carbs INT NOT NULL DEFAULT 0,
    protein INT NOT NULL DEFAULT 0,
    fat INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_meals_diary FOREIGN KEY (diary_id) REFERENCES diaries(id)
);

CREATE TABLE activities (
    id BIGINT NOT NULL AUTO_INCREMENT,
    diary_id BIGINT NOT NULL,
    activity_amount INT NOT NULL,
    memo VARCHAR(200) NULL,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_activities_diary FOREIGN KEY (diary_id) REFERENCES diaries(id)
);

CREATE INDEX idx_diaries_user_date ON diaries(user_id, record_date);
CREATE INDEX idx_meals_diary ON meals(diary_id);
CREATE INDEX idx_activities_diary ON activities(diary_id);
