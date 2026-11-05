-- =============================================================================
-- Gamer Events · Script de inserción de datos (MySQL 8.4)
-- =============================================================================
-- Datos iniciales: usuarios de prueba, eventos y marcas.
-- Ejecutar después de 01-schema.sql.
--
-- Las contraseñas se guardan solo como hash BCrypt, el mismo formato
-- que genera la aplicación al registrar un usuario. Las contraseñas en texto
-- plano NO están en el repositorio: se entregan en el informe.
--
-- Las fechas de los eventos son relativas al día en que se carga el script,
-- para que siempre aparezcan como próximos.
-- =============================================================================

SET NAMES utf8mb4;

-- -----------------------------------------------------------------------------
-- Usuarios: 3 con rol USER (misma contraseña) y 1 con rol ADMIN (otra contraseña)
-- -----------------------------------------------------------------------------
INSERT INTO gamer_users (name, email, password_hash, role, nickname, avatar, games, notifications) VALUES
    ('Alex Player', 'gamer1@example.test', '$2a$10$3tbGDj2FUIjxo28jv4bSdeKUx3beytZCnIrSkZAY5Bga3E0HyP/42',  'USER',  '', 'controller', '', 0),
    ('Sam Pixel',   'gamer2@example.test', '$2a$10$3tbGDj2FUIjxo28jv4bSdeKUx3beytZCnIrSkZAY5Bga3E0HyP/42',  'USER',  '', 'controller', '', 0),
    ('Dani Quest',  'gamer3@example.test', '$2a$10$3tbGDj2FUIjxo28jv4bSdeKUx3beytZCnIrSkZAY5Bga3E0HyP/42',  'USER',  '', 'controller', '', 0),
    ('Admin Nexus', 'admin@example.test',  '$2a$10$/mHglwuSdZzd9YPrODPZue4X33ipsPKr4Yei/Lq5.QL/2dhzqlzl2', 'ADMIN', '', 'controller', '', 0);

-- -----------------------------------------------------------------------------
-- Eventos: 4 presenciales y 2 online
-- -----------------------------------------------------------------------------
INSERT INTO gamer_events (title, category, game, country, modality, location, address, access_url, starts_at,
                          description, organizers, services, exhibitors, requirements, registration_info,
                          image_url, featured) VALUES
    ('Nexus Arena · Torneo de equipos', 'Esports', 'Valorant', 'Chile', 'PRESENCIAL',
     'Santiago', 'Centro Demo Nexus, Avenida Virtual 120', NULL,
     TIMESTAMP(CURDATE() + INTERVAL 10 DAY, '18:30:00'),
     'Encuentro ficticio de equipos con partidas competitivas y espacios para conocer jugadores.',
     'Comunidad Nexus (organización ficticia)',
     'Estacionamiento: disponible.\nLocomoción colectiva: parada cercana al recinto.\nComidas: patio de comidas.\nAlojamiento: no incluido.',
     'Pixel Forge y equipos invitados (ficticios)',
     'Equipos de 5 jugadores mayores de 16 años. Cada jugador lleva sus periféricos.',
     'Inscripción por equipo hasta 3 días antes del evento. Cupo para 16 equipos. Entrada general gratuita.',
     '/images/events/nexus-arena.svg', TRUE),

    ('Indie Play · Mundos por descubrir', 'Indie', 'Hollow Knight', 'Chile', 'PRESENCIAL',
     'Valparaíso', 'Galería Demo Pixel, Paseo Digital 45', NULL,
     TIMESTAMP(CURDATE() + INTERVAL 17 DAY, '11:00:00'),
     'Muestra ficticia de videojuegos independientes, prototipos y conversaciones con sus creadores.',
     'Colectivo Indie Play (ficticio)',
     'Estacionamiento: no disponible.\nLocomoción colectiva: acceso peatonal desde terminal cercano.\nComidas: cafetería.\nAlojamiento: opciones externas no incluidas.',
     'Estudio Bit Sur y Taller 8 Bits (ficticios)',
     'Abierto a todo público. Menores de 14 años asisten con un adulto.',
     'Entrada liberada con registro en la puerta. Los desarrolladores postulan su demo hasta 10 días antes.',
     '/images/events/indie-play.svg', TRUE),

    ('Co-op Weekend · Juguemos juntos', 'Comunidad', 'It Takes Two', 'Chile', 'PRESENCIAL',
     'Concepción', 'Espacio Demo Co-op, Calle Arcade 88', NULL,
     TIMESTAMP(CURDATE() + INTERVAL 24 DAY, '15:00:00'),
     'Jornada ficticia de juegos cooperativos y desafíos para jugadores de todos los niveles.',
     'Red Co-op (ficticia)',
     'Estacionamiento: cupos limitados.\nLocomoción colectiva: parada a dos cuadras.\nComidas: puestos habilitados.\nAlojamiento: no incluido.',
     'Clubes de juegos locales (ficticios)',
     'Participación en parejas. No se requiere experiencia previa.',
     'Inscripción de parejas el mismo día, por orden de llegada, hasta completar 20 estaciones.',
     '/images/events/co-op-weekend.svg', FALSE),

    ('Copa Andina de Pelea', 'Esports', 'Street Fighter 6', 'Argentina', 'PRESENCIAL',
     'Buenos Aires', 'Club Demo Andino, Avenida Combo 300', NULL,
     TIMESTAMP(CURDATE() + INTERVAL 31 DAY, '17:00:00'),
     'Torneo ficticio de juegos de pelea con eliminación doble y jugadores de distintos países.',
     'Liga Andina de Pelea (organización ficticia)',
     'Estacionamiento: pagado en el recinto.\nLocomoción colectiva: subte a una cuadra.\nComidas: food trucks.\nAlojamiento: convenio con hotel cercano.',
     'Arcade Sur y Joystick Lab (ficticios)',
     'Jugadores mayores de 18 años. Se permite control o arcade stick propio.',
     'Inscripción individual con cuota de participación. Cupo máximo de 64 jugadores.',
     '/images/events/copa-andina.svg', TRUE),

    ('Liga Online Rocket', 'Esports', 'Rocket League', 'México', 'ONLINE',
     NULL, NULL, 'https://eventos.example.test/liga-rocket',
     TIMESTAMP(CURDATE() + INTERVAL 6 DAY, '20:00:00'),
     'Liga ficticia en línea con fase de grupos y transmisión de la final.',
     'Liga Rocket Latam (organización ficticia)',
     'Transmisión en vivo, sala de voz para equipos y soporte técnico durante las partidas.',
     'Patrocinadores de periféricos (ficticios)',
     'Equipos de 3 jugadores con cuenta activa en la plataforma y conexión estable.',
     'Inscripción previa por formulario hasta 2 días antes. Las llaves se publican el día anterior.',
     '/images/events/liga-rocket.svg', FALSE),

    ('Noche de Speedrun', 'Comunidad', 'Minecraft', 'Perú', 'ONLINE',
     NULL, NULL, 'https://eventos.example.test/noche-speedrun',
     TIMESTAMP(CURDATE() + INTERVAL 13 DAY, '21:00:00'),
     'Maratón ficticia de speedruns con categorías para principiantes y expertos.',
     'Comunidad Speedrun Andes (ficticia)',
     'Transmisión en vivo, chat moderado y tabla de tiempos en línea.',
     'Creadores de contenido invitados (ficticios)',
     'Grabar la partida completa y usar una versión permitida del juego.',
     'Inscripción gratuita con el enlace de acceso. Los tiempos se envían antes del cierre de la noche.',
     '/images/events/noche-speedrun.svg', FALSE);

-- -----------------------------------------------------------------------------
-- Marcas (banners comerciales ficticios del inicio)
-- -----------------------------------------------------------------------------
INSERT INTO brand_ads (name, message) VALUES
    ('Pixel Forge', 'Periféricos para tu próxima partida. Marca ficticia de demostración.'),
    ('Nova Gear',   'Equipa tu espacio de juego. Marca ficticia de demostración.'),
    ('Bit Fuel',    'Energía para nuevas aventuras. Marca ficticia de demostración.');
