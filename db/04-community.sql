-- Instalaciones nuevas: 01-schema ya incluye owner_id.
-- Bases existentes: ejecutar antes migrations/2026-11-23-owner.sql.
CREATE TABLE IF NOT EXISTS event_media (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    event_id BIGINT NOT NULL,
    content_type VARCHAR(30) NOT NULL,
    content LONGBLOB NOT NULL,
    created_at DATETIME(6) NOT NULL,
    CONSTRAINT fk_media_event FOREIGN KEY (event_id) REFERENCES gamer_events(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS event_shares (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    event_id BIGINT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    CONSTRAINT uk_share_event UNIQUE (event_id),
    CONSTRAINT fk_share_event FOREIGN KEY (event_id) REFERENCES gamer_events(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
