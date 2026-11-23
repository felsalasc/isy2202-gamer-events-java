-- Ejecutar una vez sobre la base de datos existente ANTES de desplegar la nueva app.
-- Es repetible. No asigna a nadie los eventos históricos sin autor conocido.
SET @task_owner_ddl = IF(
    (SELECT COUNT(*) FROM information_schema.columns
     WHERE table_schema = DATABASE() AND table_name = 'gamer_events' AND column_name = 'owner_id') = 0,
    'ALTER TABLE gamer_events ADD COLUMN owner_id BIGINT NULL, ADD CONSTRAINT fk_event_owner FOREIGN KEY (owner_id) REFERENCES gamer_users(id)',
    'SELECT 1'
);
PREPARE task_owner_statement FROM @task_owner_ddl;
EXECUTE task_owner_statement;
DEALLOCATE PREPARE task_owner_statement;
