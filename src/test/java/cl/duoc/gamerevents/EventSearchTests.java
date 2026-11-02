package cl.duoc.gamerevents;

import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
import cl.duoc.gamerevents.model.GamerEvent;
import cl.duoc.gamerevents.repository.EventRepository;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** [Pública] Búsqueda de eventos: los 6 filtros de la pauta, sin iniciar sesión. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class EventSearchTests {
    private static final LocalDate DAY = LocalDate.now().plusDays(5);

    @Autowired
    MockMvc mvc;
    @Autowired
    EventRepository events;

    @BeforeEach
    void catalog() {
        presencial("Nexus Arena", "Esports", "Valorant", "Chile", "Santiago", DAY.atTime(18, 0));
        presencial("Indie Play", "Indie", "Hollow Knight", "Chile", "Valparaíso", DAY.plusDays(1).atTime(11, 0));
        presencial("Copa Andina", "Esports", "Street Fighter 6", "Argentina", "Buenos Aires", DAY.atTime(17, 0));
        events.save(GamerEvent.online("Liga Rocket", "Esports", "Rocket League", "México",
                "https://eventos.example.test/liga", DAY.atTime(20, 0), "Descripción privada online",
                "Organizador", "Servicios", "Expositores", "Requisitos privados", "Inscripción privada",
                "/images/events/liga-rocket.svg", false));
    }

    private void presencial(String title, String category, String game, String country, String city,
                            LocalDateTime startsAt) {
        events.save(GamerEvent.presencial(title, category, game, country, city, "Dirección privada 123", startsAt,
                "Descripción privada", "Organizador", "Servicios", "Expositores", "Requisitos privados",
                "Inscripción privada", "/images/events/nexus-arena.svg", false));
    }

    /** Búsqueda anónima: sin {@code .with(user(...))}. */
    private ResultActions search(String... params) throws Exception {
        var request = get("/eventos");
        for (int index = 0; index < params.length; index += 2) {
            request.param(params[index], params[index + 1]);
        }
        return mvc.perform(request).andExpect(status().isOk());
    }

    private static void onlyFinds(ResultActions result, String expected, String... excluded) throws Exception {
        result.andExpect(content().string(containsString(expected)));
        for (String title : excluded) {
            result.andExpect(content().string(not(containsString(title))));
        }
    }

    @Test
    void searchIsPublicAndListsAllEventsWithoutFilters() throws Exception {
        search().andExpect(view().name("events"))
                .andExpect(content().string(containsString("Nexus Arena")))
                .andExpect(content().string(containsString("Indie Play")))
                .andExpect(content().string(containsString("Copa Andina")))
                .andExpect(content().string(containsString("Liga Rocket")));
    }

    @Test
    void formOffersEveryFilterOfTheRubric() throws Exception {
        search().andExpect(content().string(containsString("name=\"name\"")))
                .andExpect(content().string(containsString("name=\"game\"")))
                .andExpect(content().string(containsString("name=\"category\"")))
                .andExpect(content().string(containsString("name=\"country\"")))
                .andExpect(content().string(containsString("name=\"city\"")))
                .andExpect(content().string(containsString("name=\"date\"")))
                .andExpect(content().string(containsString("name=\"modality\"")));
    }

    @Test
    void nameMatchesPartiallyIgnoringCase() throws Exception {
        onlyFinds(search("name", "  nEXus "), "Nexus Arena", "Indie Play", "Copa Andina", "Liga Rocket");
    }

    @Test
    void nameTreatsLikeWildcardsAsPlainText() throws Exception {
        // Sin escapar, "%" en un LIKE coincidiría con todos los eventos.
        search("name", "%").andExpect(content().string(containsString("No hay eventos que coincidan")));
        search("name", "_").andExpect(content().string(containsString("No hay eventos que coincidan")));
    }

    @Test
    void filtersByGame() throws Exception {
        onlyFinds(search("game", "Hollow Knight"), "Indie Play", "Nexus Arena", "Copa Andina", "Liga Rocket");
    }

    @Test
    void filtersByCategory() throws Exception {
        onlyFinds(search("category", "Indie"), "Indie Play", "Nexus Arena", "Copa Andina", "Liga Rocket");
    }

    @Test
    void filtersByCountry() throws Exception {
        onlyFinds(search("country", "Argentina"), "Copa Andina", "Nexus Arena", "Indie Play", "Liga Rocket");
    }

    @Test
    void filtersByCity() throws Exception {
        onlyFinds(search("city", "Valparaíso"), "Indie Play", "Nexus Arena", "Copa Andina", "Liga Rocket");
    }

    @Test
    void filtersByDate() throws Exception {
        onlyFinds(search("date", DAY.plusDays(1).toString()), "Indie Play", "Nexus Arena", "Copa Andina",
                "Liga Rocket");
    }

    @Test
    void filtersByModality() throws Exception {
        onlyFinds(search("modality", "ONLINE"), "Liga Rocket", "Nexus Arena", "Indie Play", "Copa Andina");
        search("modality", "PRESENCIAL")
                .andExpect(content().string(containsString("Nexus Arena")))
                .andExpect(content().string(not(containsString("Liga Rocket"))));
    }

    @Test
    void combinesFilters() throws Exception {
        onlyFinds(search("category", "Esports", "country", "Chile", "modality", "PRESENCIAL"), "Nexus Arena",
                "Indie Play", "Copa Andina", "Liga Rocket");
    }

    @Test
    void unknownModalityIsBadRequest() throws Exception {
        mvc.perform(get("/eventos").param("modality", "HOLOGRAMA")).andExpect(status().isBadRequest());
    }

    @Test
    void tooLongTextFilterIsBadRequest() throws Exception {
        mvc.perform(get("/eventos").param("name", "x".repeat(141))).andExpect(status().isBadRequest());
    }

    @Test
    void resultsShowSummaryWithGameAndModalityButNoPrivateDetail() throws Exception {
        search().andExpect(content().string(containsString("Valorant")))
                .andExpect(content().string(containsString("Online · México")))
                .andExpect(content().string(not(containsString("Descripción privada"))))
                .andExpect(content().string(not(containsString("Dirección privada"))))
                .andExpect(content().string(not(containsString("Requisitos privados"))))
                .andExpect(content().string(not(containsString("Inscripción privada"))))
                .andExpect(content().string(not(containsString("https://eventos.example.test/liga"))));
    }

    @Test
    void cityOptionsDoNotIncludeOnlineEventsWithoutCity() throws Exception {
        // Los eventos online no tienen ciudad: un null en la lista sería una opción vacía en el select.
        search().andExpect(model().attribute("cities", contains("Buenos Aires", "Santiago", "Valparaíso")))
                .andExpect(model().attribute("countries", contains("Argentina", "Chile", "México")));
    }
}
