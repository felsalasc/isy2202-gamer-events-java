-- Semana 5. También se puede aplicar manualmente a una base ya existente.
-- No borra usuarios, eventos ni valoraciones previas.
CREATE TABLE IF NOT EXISTS event_comments (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    event_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    text VARCHAR(1000) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    CONSTRAINT fk_comment_event FOREIGN KEY (event_id) REFERENCES gamer_events(id),
    CONSTRAINT fk_comment_user FOREIGN KEY (user_id) REFERENCES gamer_users(id),
    INDEX ix_comment_event_date (event_id, created_at, id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS event_ratings (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    event_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    score INT NOT NULL,
    CONSTRAINT uk_rating_event_user UNIQUE (event_id, user_id),
    CONSTRAINT ck_rating_score CHECK (score BETWEEN 1 AND 5),
    CONSTRAINT fk_rating_event FOREIGN KEY (event_id) REFERENCES gamer_events(id),
    CONSTRAINT fk_rating_user FOREIGN KEY (user_id) REFERENCES gamer_users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
