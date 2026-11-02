package cl.duoc.gamerevents;

import java.time.LocalDateTime;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import cl.duoc.gamerevents.config.Constants;
import cl.duoc.gamerevents.model.GamerEvent;
import cl.duoc.gamerevents.model.RegistrationForm;
import cl.duoc.gamerevents.repository.EventRepository;
import cl.duoc.gamerevents.service.UserService;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * [Privada] {@code GET /api/events/{id}}: el detalle completo solo con un token JWT obtenido en
 * {@code POST /api/auth/login}. Recorre el flujo completo de la pauta: login → token → API privada.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class EventApiDetailTests {
    private static final String EMAIL = "detalle.api@example.test";
    private static final String PASSWORD = "ClaveSegura2026!";
    private static final LocalDateTime STARTS_AT = LocalDateTime.of(2026, 12, 5, 18, 30);

    @Autowired
    MockMvc mvc;
    @Autowired
    EventRepository events;
    @Autowired
    UserService users;

    private long presencialId;
    private long onlineId;

    @BeforeEach
    void data() {
        RegistrationForm form = new RegistrationForm();
        form.setName("Detalle Api");
        form.setEmail(EMAIL);
        form.setPassword(PASSWORD);
        form.setConfirmPassword(PASSWORD);
        users.register(form);
        presencialId = events.save(GamerEvent.presencial("Nexus Arena", "Esports", "Valorant", "Chile", "Santiago",
                "Avenida Virtual 120", STARTS_AT, "Partidas competitivas de equipos.", "Comunidad Nexus",
                "Estacionamiento disponible", "Pixel Forge", "Equipos de 5 jugadores", "Inscripción por equipo",
                "/images/events/nexus-arena.svg", true)).getId();
        onlineId = events.save(GamerEvent.online("Liga Rocket", "Esports", "Rocket League", "México",
                "https://eventos.example.test/liga-rocket", STARTS_AT, "Liga en línea.", "Liga Rocket Latam",
                "Transmisión en vivo", "Patrocinadores", "Equipos de 3 jugadores", "Inscripción por formulario",
                "/images/events/liga-rocket.svg", false)).getId();
    }

    /** Token real: el mismo camino que usa un cliente de la API. */
    private String bearerToken() throws Exception {
        String response = mvc.perform(post(Constants.LOGIN_URL).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + EMAIL + "\",\"password\":\"" + PASSWORD + "\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return "Bearer " + JsonPath.read(response, "$.token");
    }

    @Test
    void withoutTokenGets401() throws Exception {
        mvc.perform(get("/api/events/" + presencialId))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Se requiere un token Bearer."));
    }

    @Test
    void withInvalidTokenGets401() throws Exception {
        mvc.perform(get("/api/events/" + presencialId).header("Authorization", "Bearer token-falso"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Token inválido o expirado."));
    }

    @Test
    void webSessionDoesNotOpenPrivateApi() throws Exception {
        mvc.perform(get("/api/events/" + presencialId).with(user(EMAIL))).andExpect(status().isUnauthorized());
    }

    @Test
    void tokenFromLoginOpensFullPresencialDetail() throws Exception {
        mvc.perform(get("/api/events/" + presencialId).header("Authorization", bearerToken()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(presencialId))
                .andExpect(jsonPath("$.title").value("Nexus Arena"))
                .andExpect(jsonPath("$.description").value("Partidas competitivas de equipos."))   // descripción
                .andExpect(jsonPath("$.game").value("Valorant"))                                    // videojuego
                .andExpect(jsonPath("$.startsAt").value("2026-12-05T18:30:00"))                     // fecha y hora
                .andExpect(jsonPath("$.city").value("Santiago"))                                    // ubicación
                .andExpect(jsonPath("$.country").value("Chile"))
                .andExpect(jsonPath("$.address").value("Avenida Virtual 120"))
                .andExpect(jsonPath("$.accessUrl").value(nullValue()))
                .andExpect(jsonPath("$.organizers").value("Comunidad Nexus"))                       // organizador
                .andExpect(jsonPath("$.requirements").value("Equipos de 5 jugadores"))              // requisitos
                .andExpect(jsonPath("$.registrationInfo").value("Inscripción por equipo"))          // inscripción
                .andExpect(jsonPath("$.imageUrl").value("/images/events/nexus-arena.svg"))          // fotografía
                .andExpect(jsonPath("$.modality").value("PRESENCIAL"))
                .andExpect(jsonPath("$.services").value("Estacionamiento disponible"))
                .andExpect(jsonPath("$.exhibitors").value("Pixel Forge"));
    }

    @Test
    void onlineDetailHasAccessLinkAndNoAddress() throws Exception {
        mvc.perform(get("/api/events/" + onlineId).header("Authorization", bearerToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.modality").value("ONLINE"))
                .andExpect(jsonPath("$.accessUrl").value("https://eventos.example.test/liga-rocket"))
                .andExpect(jsonPath("$.address").value(nullValue()))
                .andExpect(jsonPath("$.city").value(nullValue()));
    }

    @Test
    void missingEventGets404WithApiError() throws Exception {
        mvc.perform(get("/api/events/99999999").header("Authorization", bearerToken()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Evento no encontrado."));
    }

    @Test
    void nonNumericIdGets400WithApiError() throws Exception {
        mvc.perform(get("/api/events/abc").header("Authorization", bearerToken()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Solicitud inválida."));
    }

    @Test
    void webDetailOfMissingEventStillRendersHtml404() throws Exception {
        // La misma excepción en la web: página de error, no JSON.
        mvc.perform(get("/eventos/99999999").with(user(EMAIL)))
                .andExpect(status().isNotFound())
                .andExpect(content().string(org.hamcrest.Matchers.not(containsString("\"error\""))));
    }
}
