CREATE TABLE period_records (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_period_records_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
CREATE INDEX idx_period_user_start ON period_records(user_id, start_date);

ALTER TABLE uploaded_images ADD COLUMN expires_at TIMESTAMP(6) NULL;
CREATE INDEX idx_uploaded_images_expires ON uploaded_images(expires_at);

CREATE TABLE ai_reports (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    from_date DATE NOT NULL,
    to_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL,
    summary TEXT NULL,
    priorities_json TEXT NULL,
    methods_json TEXT NULL,
    model_version VARCHAR(100) NULL,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_ai_reports_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
CREATE INDEX idx_ai_report_user_created ON ai_reports(user_id, created_at);

CREATE TABLE ai_conversations (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    title VARCHAR(100) NOT NULL,
    last_message_at TIMESTAMP(6) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_ai_conversations_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
CREATE INDEX idx_ai_conversation_user_last_message ON ai_conversations(user_id, last_message_at);

CREATE TABLE ai_chat_messages (
    id BIGINT NOT NULL AUTO_INCREMENT,
    conversation_id BIGINT NOT NULL,
    role VARCHAR(20) NOT NULL,
    content TEXT NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_ai_chat_messages_conversation FOREIGN KEY (conversation_id)
        REFERENCES ai_conversations(id) ON DELETE CASCADE
);
CREATE INDEX idx_chat_message_conversation ON ai_chat_messages(conversation_id, created_at);

CREATE TABLE nutrition_analyses (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    record_date DATE NOT NULL,
    meal_type VARCHAR(20) NOT NULL,
    input_type VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    image_url VARCHAR(1000) NULL,
    input_text TEXT NULL,
    model_version VARCHAR(100) NULL,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_nutrition_analyses_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
CREATE INDEX idx_nutrition_analysis_user_date ON nutrition_analyses(user_id, record_date);

CREATE TABLE nutrition_draft_foods (
    id BIGINT NOT NULL AUTO_INCREMENT,
    analysis_id BIGINT NOT NULL,
    food_name VARCHAR(100) NOT NULL,
    amount DECIMAL(8,1) NULL,
    amount_unit VARCHAR(20) NULL,
    kcal INT NULL,
    carbs INT NULL,
    protein INT NULL,
    fat INT NULL,
    confidence DECIMAL(4,3) NULL,
    source VARCHAR(20) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_nutrition_draft_foods_analysis FOREIGN KEY (analysis_id)
        REFERENCES nutrition_analyses(id) ON DELETE CASCADE
);
CREATE INDEX idx_nutrition_food_analysis ON nutrition_draft_foods(analysis_id);

ALTER TABLE meals ADD COLUMN nutrition_analysis_id BIGINT NULL;
ALTER TABLE meals ADD COLUMN source_image_url VARCHAR(1000) NULL;
ALTER TABLE meals ADD CONSTRAINT fk_meals_nutrition_analysis
    FOREIGN KEY (nutrition_analysis_id) REFERENCES nutrition_analyses(id);
CREATE INDEX idx_meals_nutrition_analysis ON meals(nutrition_analysis_id);
