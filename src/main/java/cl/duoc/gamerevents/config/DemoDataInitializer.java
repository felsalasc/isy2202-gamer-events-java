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
            events.save(new GamerEvent("Nexus Arena · Torneo de equipos", "Esports", "Santiago", "Centro Demo Nexus, Avenida Virtual 120",
                    today.plusDays(10).atTime(18, 30), "Encuentro ficticio de equipos con partidas competitivas y espacios para conocer jugadores.",
                    "Comunidad Nexus (organización ficticia)", "Estacionamiento: disponible.\nLocomoción colectiva: parada cercana al recinto.\nComidas: patio de comidas.\nAlojamiento: no incluido.",
                    "Pixel Forge y equipos invitados (ficticios)", true));
            events.save(new GamerEvent("Indie Play · Mundos por descubrir", "Indie", "Valparaíso", "Galería Demo Pixel, Paseo Digital 45",
                    today.plusDays(17).atTime(11, 0), "Muestra ficticia de videojuegos independientes, prototipos y conversaciones con sus creadores.",
                    "Colectivo Indie Play (ficticio)", "Estacionamiento: no disponible.\nLocomoción colectiva: acceso peatonal desde terminal cercano.\nComidas: cafetería.\nAlojamiento: opciones externas no incluidas.",
                    "Estudio Bit Sur y Taller 8 Bits (ficticios)", true));
            events.save(new GamerEvent("Co-op Weekend · Juguemos juntos", "Comunidad", "Concepción", "Espacio Demo Co-op, Calle Arcade 88",
                    today.plusDays(24).atTime(15, 0), "Jornada ficticia de juegos cooperativos y desafíos para jugadores de todos los niveles.",
                    "Red Co-op (ficticia)", "Estacionamiento: cupos limitados.\nLocomoción colectiva: parada a dos cuadras.\nComidas: puestos habilitados.\nAlojamiento: no incluido.",
                    "Clubes de juegos locales (ficticios)", false));
        }
        if (brands.count() == 0) {
            brands.save(new BrandAd("Pixel Forge", "Periféricos para tu próxima partida. Marca ficticia de demostración."));
            brands.save(new BrandAd("Nova Gear", "Equipa tu espacio de juego. Marca ficticia de demostración."));
            brands.save(new BrandAd("Bit Fuel", "Energía para nuevas aventuras. Marca ficticia de demostración."));
        }
    }
}
