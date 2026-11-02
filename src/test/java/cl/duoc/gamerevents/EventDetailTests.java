package cl.duoc.gamerevents;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
import cl.duoc.gamerevents.model.GamerEvent;
import cl.duoc.gamerevents.repository.EventRepository;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class EventDetailTests {
    private static final LocalDateTime STARTS_AT = LocalDateTime.of(2026, 12, 5, 18, 30);

    @Autowired
    MockMvc mvc;
    @Autowired
    EventRepository events;

    private long presencial() {
        return events.save(GamerEvent.presencial("Nexus Arena", "Esports", "Valorant", "Chile", "Santiago",
                "Avenida Virtual 120", STARTS_AT, "Partidas competitivas de equipos.", "Comunidad Nexus",
                "Estacionamiento disponible", "Pixel Forge", "Equipos de 5 jugadores mayores de 16 años",
                "Inscripción por equipo hasta 3 días antes", "/images/events/nexus-arena.svg", true)).getId();
    }

    private long online() {
        return events.save(GamerEvent.online("Liga Rocket", "Esports", "Rocket League", "México",
                "https://eventos.example.test/liga-rocket", STARTS_AT, "Liga en línea con transmisión.",
                "Liga Rocket Latam", "Transmisión en vivo", "Patrocinadores", "Equipos de 3 jugadores",
                "Inscripción previa por formulario", "/images/events/liga-rocket.svg", false)).getId();
    }

    private ResultActions detailAsUser(long id) throws Exception {
        return mvc.perform(get("/eventos/" + id).with(user("gamer1@example.test")))
                .andExpect(status().isOk()).andExpect(view().name("event-detail"));
    }

    @Test
    void detailShowsEveryFieldRequiredByTheRubric() throws Exception {
        detailAsUser(presencial())
                .andExpect(content().string(containsString("Partidas competitivas de equipos."))) // descripción
                .andExpect(content().string(containsString("Valorant"))) // videojuego
                .andExpect(content().string(containsString("05/12/2026"))) // fecha
                .andExpect(content().string(containsString("18:30"))) // hora
                .andExpect(content().string(containsString("Avenida Virtual 120"))) // ubicación
                .andExpect(content().string(containsString("Santiago, Chile")))
                .andExpect(content().string(containsString("Comunidad Nexus"))) // organizador
                .andExpect(content().string(containsString("Equipos de 5 jugadores mayores de 16 años"))) // requisitos
                .andExpect(content().string(containsString("Inscripción por equipo hasta 3 días antes"))) // inscripción
                .andExpect(content().string(containsString("Presencial"))); // modalidad
    }

    @Test
    void detailShowsEventImageWithAccessibleAltText() throws Exception {
        detailAsUser(presencial())
                .andExpect(content().string(containsString("src=\"/images/events/nexus-arena.svg\"")))
                .andExpect(content().string(containsString("alt=\"Imagen del evento Nexus Arena\"")));
    }

    @Test
    void onlineDetailShowsSafeAccessLinkInsteadOfAddress() throws Exception {
        detailAsUser(online())
                .andExpect(content().string(containsString("Online")))
                .andExpect(content().string(containsString("href=\"https://eventos.example.test/liga-rocket\"")))
                .andExpect(content().string(containsString("rel=\"noopener noreferrer\"")))
                .andExpect(content().string(containsString("Enlace de acceso")))
                .andExpect(content().string(not(containsString("Dirección"))));
    }

    @Test
    void presencialDetailHasNoAccessLink() throws Exception {
        detailAsUser(presencial())
                .andExpect(content().string(containsString("Dirección")))
                .andExpect(content().string(not(containsString("Enlace de acceso"))));
    }

    @Test
    void detailStaysPrivate() throws Exception {
        mvc.perform(get("/eventos/" + online())).andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }
}
