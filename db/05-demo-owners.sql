-- Asignación explícita SOLO de los seis eventos ficticios originales.
-- No modifica propietarios existentes ni atribuye otros eventos.
UPDATE gamer_events e
JOIN (
    SELECT 'Nexus Arena · Torneo de equipos' AS title, '/images/events/nexus-arena.svg' AS image_url, 'gamer1@example.test' AS email
    UNION ALL SELECT 'Indie Play · Mundos por descubrir', '/images/events/indie-play.svg', 'gamer2@example.test'
    UNION ALL SELECT 'Co-op Weekend · Juguemos juntos', '/images/events/co-op-weekend.svg', 'gamer3@example.test'
    UNION ALL SELECT 'Copa Andina de Pelea', '/images/events/copa-andina.svg', 'gamer1@example.test'
    UNION ALL SELECT 'Liga Online Rocket', '/images/events/liga-rocket.svg', 'gamer2@example.test'
    UNION ALL SELECT 'Noche de Speedrun', '/images/events/noche-speedrun.svg', 'gamer3@example.test'
) demo ON demo.title = e.title AND demo.image_url = e.image_url
JOIN gamer_users u ON u.email = demo.email
SET e.owner_id = u.id
WHERE e.owner_id IS NULL;
