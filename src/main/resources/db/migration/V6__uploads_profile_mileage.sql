ALTER TABLE user_profiles ADD COLUMN focus_areas TEXT NULL;
ALTER TABLE user_profiles ADD COLUMN recovery_areas TEXT NULL;
ALTER TABLE user_profiles ADD COLUMN skin_concerns TEXT NULL;

CREATE TABLE uploaded_images (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    purpose VARCHAR(30) NOT NULL,
    content_type VARCHAR(50) NOT NULL,
    file_size BIGINT NOT NULL,
    data LONGBLOB NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_uploaded_images_user FOREIGN KEY (user_id) REFERENCES users(id)
);
CREATE INDEX idx_uploaded_images_user ON uploaded_images(user_id);

CREATE TABLE mileage_wallets (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    balance INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_mileage_wallet_user UNIQUE (user_id),
    CONSTRAINT fk_mileage_wallet_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE mileage_histories (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    type VARCHAR(20) NOT NULL,
    reason VARCHAR(40) NOT NULL,
    amount INT NOT NULL,
    balance_after INT NOT NULL,
    reference_id VARCHAR(100) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_mileage_user_reference UNIQUE (user_id, reference_id),
    CONSTRAINT fk_mileage_history_user FOREIGN KEY (user_id) REFERENCES users(id)
);
CREATE INDEX idx_mileage_history_user_created ON mileage_histories(user_id, created_at);
