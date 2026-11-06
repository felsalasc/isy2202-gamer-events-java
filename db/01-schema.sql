-- =============================================================================
-- Gamer Events · Script de creación de la base de datos (MySQL 8.4)
-- =============================================================================
-- Crea las tablas que usa la aplicación. Los tipos son los mismos que genera
-- Hibernate a partir de las entidades JPA, porque la aplicación arranca con
-- spring.jpa.hibernate.ddl-auto=validate y no modifica el esquema por su cuenta.
--
-- Uso manual (sobre la base gamer_events ya creada):
--   mysql -u gamer_events -p gamer_events < db/01-schema.sql
--   mysql -u gamer_events -p gamer_events < db/02-data.sql
--
-- Con Docker Compose, MySQL ejecuta este archivo automáticamente la primera vez
-- que se crea el volumen mysql-data.
--
-- ATENCIÓN: borra las tablas si ya existen, para que el script sea repetible.
-- =============================================================================

DROP TABLE IF EXISTS gamer_users;
DROP TABLE IF EXISTS gamer_events;
DROP TABLE IF EXISTS brand_ads;

-- Usuarios. La contraseña se guarda solo como hash BCrypt.
-- role: USER (usuario de la comunidad) o ADMIN (acceso a /admin).
CREATE TABLE gamer_users (
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    name           VARCHAR(80)  NOT NULL,
    email          VARCHAR(254) NOT NULL,
    password_hash  VARCHAR(100) NOT NULL,
    role           ENUM('ADMIN', 'USER') NOT NULL,
    nickname       VARCHAR(40)  NOT NULL,
    avatar         VARCHAR(20)  NOT NULL,
    games          VARCHAR(200) NOT NULL,
    notifications  BIT          NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_gamer_email UNIQUE (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Eventos gamer. Uno presencial tiene location (ciudad) y address;
-- uno online tiene access_url (https).
CREATE TABLE gamer_events (
    id                 BIGINT        NOT NULL AUTO_INCREMENT,
    title              VARCHAR(140)  NOT NULL,
    category           VARCHAR(60)   NOT NULL,
    game               VARCHAR(80)   NOT NULL,
    country            VARCHAR(60)   NOT NULL,
    modality           ENUM('ONLINE', 'PRESENCIAL') NOT NULL,
    location           VARCHAR(100),
    address            VARCHAR(200),
    access_url         VARCHAR(300),
    starts_at          DATETIME(6)   NOT NULL,
    description        VARCHAR(4000) NOT NULL,
    organizers         VARCHAR(1000) NOT NULL,
    services           VARCHAR(2000) NOT NULL,
    exhibitors         VARCHAR(2000) NOT NULL,
    requirements       VARCHAR(1000) NOT NULL,
    registration_info  VARCHAR(1000) NOT NULL,
    image_url          VARCHAR(200)  NOT NULL,
    featured           BIT           NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Banners de marcas (ficticias) que se muestran en el inicio.
CREATE TABLE brand_ads (
    id       BIGINT       NOT NULL AUTO_INCREMENT,
    name     VARCHAR(80)  NOT NULL,
    message  VARCHAR(200) NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
