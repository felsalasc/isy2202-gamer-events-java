package cl.duoc.gamerevents.config;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import cl.duoc.gamerevents.model.*;
import cl.duoc.gamerevents.repository.*;

@Component
@Profile("demo")
public class DemoDataInitializer implements ApplicationRunner {
    private final GamerUserRepository users;
    private final EventRepository events;
    private final BrandAdRepository brands;
    private final PasswordEncoder encoder;
    private final String password;
    public DemoDataInitializer(GamerUserRepository users, EventRepository events, BrandAdRepository brands,
                               PasswordEncoder encoder, @Value("${app.demo.password:}") String password) {
        this.users = users; this.events = events; this.brands = brands; this.encoder = encoder; this.password = password;
    }
    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (password.isBlank() || password.length() < 12 || password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new IllegalStateException("El perfil demo requiere GAMER_DEMO_PASSWORD de al menos 12 caracteres y hasta 72 bytes.");
        }
        String[] names = {"Alex Player", "Sam Pixel", "Dani Quest"};
        for (int index = 0; index < names.length; index++) {
            String email = "gamer" + (index + 1) + "@example.test";
            if (!users.existsByEmail(email)) {
                users.save(new GamerUser(names[index], email, encoder.encode(password)));
            }
        }
        if (events.count() == 0) {
            LocalDate today = LocalDate.now();
            events.save(GamerEvent.presencial("Nexus Arena · Torneo de equipos", "Esports", "Valorant", "Chile",
                    "Santiago", "Centro Demo Nexus, Avenida Virtual 120", today.plusDays(10).atTime(18, 30),
                    "Encuentro ficticio de equipos con partidas competitivas y espacios para conocer jugadores.",
                    "Comunidad Nexus (organización ficticia)",
                    "Estacionamiento: disponible.\nLocomoción colectiva: parada cercana al recinto.\nComidas: patio de comidas.\nAlojamiento: no incluido.",
                    "Pixel Forge y equipos invitados (ficticios)",
                    "Equipos de 5 jugadores mayores de 16 años. Cada jugador lleva sus periféricos.",
                    "Inscripción por equipo hasta 3 días antes del evento. Cupo para 16 equipos. Entrada general gratuita.",
                    "/images/events/nexus-arena.svg", true));
            events.save(GamerEvent.presencial("Indie Play · Mundos por descubrir", "Indie", "Hollow Knight", "Chile",
                    "Valparaíso", "Galería Demo Pixel, Paseo Digital 45", today.plusDays(17).atTime(11, 0),
                    "Muestra ficticia de videojuegos independientes, prototipos y conversaciones con sus creadores.",
                    "Colectivo Indie Play (ficticio)",
                    "Estacionamiento: no disponible.\nLocomoción colectiva: acceso peatonal desde terminal cercano.\nComidas: cafetería.\nAlojamiento: opciones externas no incluidas.",
                    "Estudio Bit Sur y Taller 8 Bits (ficticios)",
                    "Abierto a todo público. Menores de 14 años asisten con un adulto.",
                    "Entrada liberada con registro en la puerta. Los desarrolladores postulan su demo hasta 10 días antes.",
                    "/images/events/indie-play.svg", true));
            events.save(GamerEvent.presencial("Co-op Weekend · Juguemos juntos", "Comunidad", "It Takes Two", "Chile",
                    "Concepción", "Espacio Demo Co-op, Calle Arcade 88", today.plusDays(24).atTime(15, 0),
                    "Jornada ficticia de juegos cooperativos y desafíos para jugadores de todos los niveles.",
                    "Red Co-op (ficticia)",
                    "Estacionamiento: cupos limitados.\nLocomoción colectiva: parada a dos cuadras.\nComidas: puestos habilitados.\nAlojamiento: no incluido.",
                    "Clubes de juegos locales (ficticios)",
                    "Participación en parejas. No se requiere experiencia previa.",
                    "Inscripción de parejas el mismo día, por orden de llegada, hasta completar 20 estaciones.",
                    "/images/events/co-op-weekend.svg", false));
            events.save(GamerEvent.presencial("Copa Andina de Pelea", "Esports", "Street Fighter 6", "Argentina",
                    "Buenos Aires", "Club Demo Andino, Avenida Combo 300", today.plusDays(31).atTime(17, 0),
                    "Torneo ficticio de juegos de pelea con eliminación doble y jugadores de distintos países.",
                    "Liga Andina de Pelea (organización ficticia)",
                    "Estacionamiento: pagado en el recinto.\nLocomoción colectiva: subte a una cuadra.\nComidas: food trucks.\nAlojamiento: convenio con hotel cercano.",
                    "Arcade Sur y Joystick Lab (ficticios)",
                    "Jugadores mayores de 18 años. Se permite control o arcade stick propio.",
                    "Inscripción individual con cuota de participación. Cupo máximo de 64 jugadores.",
                    "/images/events/copa-andina.svg", true));
            events.save(GamerEvent.online("Liga Online Rocket", "Esports", "Rocket League", "México",
                    "https://eventos.example.test/liga-rocket", today.plusDays(6).atTime(20, 0),
                    "Liga ficticia en línea con fase de grupos y transmisión de la final.",
                    "Liga Rocket Latam (organización ficticia)",
                    "Transmisión en vivo, sala de voz para equipos y soporte técnico durante las partidas.",
                    "Patrocinadores de periféricos (ficticios)",
                    "Equipos de 3 jugadores con cuenta activa en la plataforma y conexión estable.",
                    "Inscripción previa por formulario hasta 2 días antes. Las llaves se publican el día anterior.",
                    "/images/events/liga-rocket.svg", false));
            events.save(GamerEvent.online("Noche de Speedrun", "Comunidad", "Minecraft", "Perú",
                    "https://eventos.example.test/noche-speedrun", today.plusDays(13).atTime(21, 0),
                    "Maratón ficticia de speedruns con categorías para principiantes y expertos.",
                    "Comunidad Speedrun Andes (ficticia)",
                    "Transmisión en vivo, chat moderado y tabla de tiempos en línea.",
                    "Creadores de contenido invitados (ficticios)",
                    "Grabar la partida completa y usar una versión permitida del juego.",
                    "Inscripción gratuita con el enlace de acceso. Los tiempos se envían antes del cierre de la noche.",
                    "/images/events/noche-speedrun.svg", false));
        }
        if (brands.count() == 0) {
            brands.save(new BrandAd("Pixel Forge", "Periféricos para tu próxima partida. Marca ficticia de demostración."));
            brands.save(new BrandAd("Nova Gear", "Equipa tu espacio de juego. Marca ficticia de demostración."));
            brands.save(new BrandAd("Bit Fuel", "Energía para nuevas aventuras. Marca ficticia de demostración."));
        }
    }
}
