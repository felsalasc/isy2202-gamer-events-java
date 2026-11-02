package cl.duoc.gamerevents;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class EventJourneysTests {
    @Autowired
    MockMvc mvc;
    @Autowired
    JdbcTemplate jdbc;

    private void event(String title, String category, String location, LocalDate date, boolean featured) {
        jdbc.update(
                "insert into gamer_events(title,category,game,country,modality,location,address,starts_at,description,"
                        + "organizers,services,exhibitors,requirements,registration_info,image_url,featured) "
                        + "values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                title, category, "Valorant", "Chile", "PRESENCIAL", location, "Calle gamer 123", date.atTime(18, 30),
                "Descripción privada del encuentro", "Equipo Gamer",
                "Estacionamiento, locomoción colectiva, comidas y alojamiento", "Estudio Indie",
                "Mayores de 16 años", "Inscripción gratuita en el recinto", "/images/events/nexus-arena.svg",
                featured);
    }

    private void onlineEvent(String title, LocalDate date) {
        jdbc.update(
                "insert into gamer_events(title,category,game,country,modality,access_url,starts_at,description,"
                        + "organizers,services,exhibitors,requirements,registration_info,image_url,featured) "
                        + "values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                title, "Esports", "Rocket League", "México", "ONLINE", "https://eventos.example.test/liga",
                date.atTime(20, 0), "Descripción privada online", "Liga Online", "Transmisión en vivo",
                "Equipos invitados", "Equipos de 3 jugadores", "Inscripción previa por formulario",
                "/images/events/liga-rocket.svg", true);
    }

    @Test
    void onlineEventCardShowsOnlineInsteadOfEmptyCity() throws Exception {
        onlineEvent("Liga online de prueba", LocalDate.now().plusDays(3));
        mvc.perform(get("/")).andExpect(status().isOk())
                .andExpect(content().string(containsString("Liga online de prueba")))
                .andExpect(content().string(containsString("Online")));
    }

    @Test
    void authenticatedUserCanBrowseEmptyCatalog() throws Exception {
        mvc.perform(get("/eventos").with(user("gamer@example.test"))).andExpect(status().isOk())
                .andExpect(content().string(containsString("No hay eventos que coincidan")));
    }

    @Test
    void combinedFiltersAndPrivateDetailUseDatabase() throws Exception {
        LocalDate date = LocalDate.now().plusDays(4);
        event("Torneo seleccionado", "Esports", "Santiago", date, true);
        event("Otra ciudad", "Esports", "Valparaíso", date, false);
        event("Otra fecha", "Esports", "Santiago", date.plusDays(1), false);
        event("Otra categoría", "Comunidad", "Santiago", date, false);
        mvc.perform(get("/eventos").with(user("gamer@example.test"))
                .param("category", "Esports").param("city", "Santiago").param("date", date.toString()))
                .andExpect(status().isOk()).andExpect(content().string(containsString("Torneo seleccionado")))
                .andExpect(content().string(not(containsString("Otra ciudad"))))
                .andExpect(content().string(not(containsString("Otra fecha"))))
                .andExpect(content().string(not(containsString("Otra categoría"))));
        Long id = jdbc.queryForObject("select id from gamer_events where title = ?", Long.class, "Torneo seleccionado");
        mvc.perform(get("/eventos/" + id).with(user("gamer@example.test"))).andExpect(status().isOk())
                .andExpect(content().string(containsString("Descripción privada")))
                .andExpect(content().string(containsString("Calle gamer 123")))
                .andExpect(content().string(containsString("Equipo Gamer")))
                .andExpect(content().string(containsString("Estudio Indie")))
                .andExpect(content().string(containsString("18:30")));
        mvc.perform(get("/eventos/" + id)).andExpect(redirectedUrl("/login"));
    }

    @Test
    void homeShowsUpcomingAndFeaturedWithoutPrivateDescriptionAndEscapesHtml() throws Exception {
        event("<script>evento</script>", "Comunidad", "Santiago", LocalDate.now().plusDays(2), true);
        event("Evento pasado", "Esports", "Santiago", LocalDate.now().minusDays(1), true);
        mvc.perform(get("/recetas")).andExpect(status().isOk())
                .andExpect(content().string(containsString("&lt;script&gt;evento&lt;/script&gt;")))
                .andExpect(content().string(not(containsString("<script>"))))
                .andExpect(content().string(not(containsString("Descripción privada"))))
                .andExpect(content().string(not(containsString("18:30"))))
                .andExpect(content().string(not(containsString("Evento pasado"))));
    }

    @Test
    void invalidDateAndMissingEventReturnClientErrors() throws Exception {
        mvc.perform(get("/eventos").with(user("gamer@example.test")).param("date", "2026-99-99"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/eventos/99999999").with(user("gamer@example.test"))).andExpect(status().isNotFound());
    }
}
