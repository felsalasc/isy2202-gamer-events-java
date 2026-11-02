package cl.duoc.gamerevents;

import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import cl.duoc.gamerevents.model.BrandAd;
import cl.duoc.gamerevents.model.GamerEvent;
import cl.duoc.gamerevents.repository.BrandAdRepository;
import cl.duoc.gamerevents.repository.EventRepository;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** APIs públicas: {@code GET /api/home} y {@code GET /api/events}, sin token y con datos de la BD. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class EventApiPublicTests {
    private static final LocalDate DAY = LocalDate.now().plusDays(5);

    @Autowired
    MockMvc mvc;
    @Autowired
    EventRepository events;
    @Autowired
    BrandAdRepository brands;

    @BeforeEach
    void catalog() {
        presencial("Nexus Arena", "Valorant", "Chile", "Santiago", DAY.atTime(18, 30), true);
        presencial("Copa Andina", "Street Fighter 6", "Argentina", "Buenos Aires", DAY.plusDays(1).atTime(17, 0), false);
        presencial("Evento pasado", "Valorant", "Chile", "Santiago", LocalDate.now().minusDays(2).atTime(10, 0), true);
        events.save(GamerEvent.online("Liga Rocket", "Esports", "Rocket League", "México",
                "https://eventos.example.test/liga", DAY.atTime(20, 0), "Descripción privada", "Organizador privado",
                "Servicios", "Expositores", "Requisitos privados", "Inscripción privada",
                "/images/events/liga-rocket.svg", false));
        brands.save(new BrandAd("Pixel Forge", "Periféricos para tu próxima partida."));
    }

    private void presencial(String title, String game, String country, String city, LocalDateTime startsAt,
                            boolean featured) {
        events.save(GamerEvent.presencial(title, "Esports", game, country, city, "Dirección privada 123", startsAt,
                "Descripción privada", "Organizador privado", "Servicios", "Expositores", "Requisitos privados",
                "Inscripción privada", "/images/events/nexus-arena.svg", featured));
    }

    @Test
    void homeIsPublicAndComesFromDatabase() throws Exception {
        mvc.perform(get("/api/home"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.upcoming[*].title", contains("Nexus Arena", "Liga Rocket", "Copa Andina")))
                .andExpect(jsonPath("$.featured[*].title", contains("Nexus Arena")))
                .andExpect(jsonPath("$.ads[0].name").value("Pixel Forge"))
                .andExpect(jsonPath("$.ads[0].message").value("Periféricos para tu próxima partida."));
    }

    @Test
    void eventsIsPublicAndListsSummaries() throws Exception {
        mvc.perform(get("/api/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(4)))
                .andExpect(jsonPath("$[?(@.title == 'Nexus Arena')].game").value("Valorant"))
                .andExpect(jsonPath("$[?(@.title == 'Nexus Arena')].city").value("Santiago"))
                .andExpect(jsonPath("$[?(@.title == 'Nexus Arena')].country").value("Chile"))
                .andExpect(jsonPath("$[?(@.title == 'Nexus Arena')].modality").value("PRESENCIAL"))
                .andExpect(jsonPath("$[?(@.title == 'Nexus Arena')].startsAt").value(DAY.atTime(18, 30).toString() + ":00"))
                .andExpect(jsonPath("$[?(@.title == 'Nexus Arena')].imageUrl").value("/images/events/nexus-arena.svg"))
                .andExpect(jsonPath("$[0].id").isNumber());
    }

    @Test
    void summariesNeverExposePrivateDetail() throws Exception {
        for (String path : new String[] {"/api/events", "/api/home"}) {
            // Exigir 200: si la ruta respondiera 401, el JSON de error no tendría datos y el test pasaría sin probar nada.
            String json = mvc.perform(get(path)).andExpect(status().isOk()).andReturn().getResponse()
                    .getContentAsString();
            org.assertj.core.api.Assertions.assertThat(json)
                    .doesNotContain("Descripción privada", "Dirección privada", "Requisitos privados",
                            "Inscripción privada", "Organizador privado", "https://eventos.example.test/liga",
                            "description", "address", "accessUrl", "requirements", "registrationInfo");
        }
    }

    @Test
    void eventsApiAppliesTheSameFiltersAsTheWebSearch() throws Exception {
        mvc.perform(get("/api/events").param("modality", "ONLINE"))
                .andExpect(jsonPath("$[*].title", contains("Liga Rocket")));
        mvc.perform(get("/api/events").param("name", "andina"))
                .andExpect(jsonPath("$[*].title", contains("Copa Andina")));
        mvc.perform(get("/api/events").param("country", "Chile").param("game", "Valorant"))
                .andExpect(jsonPath("$[*].title", containsInAnyOrder("Nexus Arena", "Evento pasado")));
        mvc.perform(get("/api/events").param("city", "Buenos Aires"))
                .andExpect(jsonPath("$[*].title", contains("Copa Andina")));
        mvc.perform(get("/api/events").param("date", DAY.plusDays(1).toString()))
                .andExpect(jsonPath("$[*].title", contains("Copa Andina")));
    }

    @Test
    void invalidFiltersGet400WithApiErrorFormat() throws Exception {
        mvc.perform(get("/api/events").param("modality", "HOLOGRAMA"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Solicitud inválida."));
        mvc.perform(get("/api/events").param("date", "2026-99-99"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Solicitud inválida."));
        mvc.perform(get("/api/events").param("name", "x".repeat(141)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Solicitud inválida."));
    }

    @Test
    void onlyGetIsPublic() throws Exception {
        mvc.perform(post("/api/events")).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/home")).andExpect(status().isUnauthorized());
    }

    @Test
    void eventDetailStaysPrivate() throws Exception {
        long id = events.findAll().get(0).getId();
        mvc.perform(get("/api/events/" + id)).andExpect(status().isUnauthorized());
    }
}
