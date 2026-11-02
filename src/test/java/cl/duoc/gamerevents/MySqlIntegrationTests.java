package cl.duoc.gamerevents;

import java.sql.Connection;
import java.time.LocalDateTime;
import javax.sql.DataSource;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mysql.MySQLContainer;
import cl.duoc.gamerevents.config.Constants;
import cl.duoc.gamerevents.model.GamerEvent;
import cl.duoc.gamerevents.model.RegistrationForm;
import cl.duoc.gamerevents.repository.EventRepository;
import cl.duoc.gamerevents.service.UserService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * La aplicación contra un MySQL 8.4 real (la misma imagen de docker-compose), levantado por
 * Testcontainers durante la prueba. Cubre lo que H2 no puede demostrar: diferencias reales de MySQL.
 * Si Docker no está disponible, estas pruebas se omiten en vez de fallar.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
@Transactional
class MySqlIntegrationTests {

    // @ServiceConnection: Spring Boot conecta la aplicación a este contenedor (reemplaza la URL de H2).
    @Container
    @ServiceConnection
    static MySQLContainer mysql = new MySQLContainer("mysql:8.4");

    @Autowired
    MockMvc mvc;
    @Autowired
    DataSource dataSource;
    @Autowired
    EventRepository events;
    @Autowired
    EntityManager entityManager;
    @Autowired
    UserService users;

    private GamerEvent save(String title, String city, String registrationInfo, LocalDateTime startsAt) {
        GamerEvent event = events.save(GamerEvent.presencial(title, "Esports", "Valorant", "Chile", city,
                "Avenida Virtual 120", startsAt, "Descripción", "Organizador", "Servicios", "Expositores",
                "Requisitos", registrationInfo, "/images/events/nexus-arena.svg", false));
        // Escribir en MySQL y vaciar la caché de JPA: la lectura siguiente viene de la base, no de memoria.
        entityManager.flush();
        entityManager.clear();
        return event;
    }

    @Test
    void runsAgainstRealMySql84() throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            assertThat(connection.getMetaData().getDatabaseProductName()).isEqualTo("MySQL");
            assertThat(connection.getMetaData().getDatabaseProductVersion()).startsWith("8.4");
        }
    }

    @Test
    void accentsEnieAndEmojiSurviveTheRoundTripWithoutUrlParameters() {
        // El emoji ocupa 4 bytes: solo se guarda si MySQL usa utf8mb4 (UTF-8 completo).
        long id = save("Torneo Ñandú", "Valparaíso", "Inscripción gratuita 🎮", LocalDateTime.of(2026, 12, 5, 18, 30))
                .getId();
        GamerEvent stored = events.findById(id).orElseThrow();
        assertThat(stored.getTitle()).isEqualTo("Torneo Ñandú");
        assertThat(stored.getLocation()).isEqualTo("Valparaíso");
        assertThat(stored.getRegistrationInfo()).isEqualTo("Inscripción gratuita 🎮");
    }

    @Test
    void localDateTimeIsStoredWithoutTimeZoneShift() {
        long id = save("Hora exacta", "Santiago", "Inscripción", LocalDateTime.of(2026, 12, 5, 18, 30)).getId();
        assertThat(events.findById(id).orElseThrow().getStartsAt()).isEqualTo(LocalDateTime.of(2026, 12, 5, 18, 30));
    }

    @Test
    void longestAllowedTextsFitInOneMySqlRow() {
        // Todos los textos al máximo de su columna y con caracteres de 2 bytes: MySQL limita los bytes por fila.
        long id = events.save(GamerEvent.presencial("á".repeat(140), "á".repeat(60), "á".repeat(80), "á".repeat(60),
                "á".repeat(100), "á".repeat(200), LocalDateTime.of(2026, 12, 5, 18, 30), "á".repeat(4000),
                "á".repeat(1000), "á".repeat(2000), "á".repeat(2000), "á".repeat(1000), "á".repeat(1000),
                "/images/events/" + "a".repeat(180), false)).getId();
        entityManager.flush();
        entityManager.clear();
        assertThat(events.findById(id).orElseThrow().getDescription()).hasSize(4000);
    }

    @Test
    void likeWildcardsAreSearchedLiterallyInMySql() throws Exception {
        // En MySQL la barra \ es especial dentro de los textos SQL: el escape del LIKE debe seguir funcionando.
        LocalDateTime day = LocalDateTime.of(2026, 12, 5, 18, 30);
        save("Descuento 50% gamer", "Santiago", "Inscripción", day);
        save("Torneo_relampago", "Santiago", "Inscripción", day);
        save("Torneo normal", "Santiago", "Inscripción", day);
        mvc.perform(get("/api/events").param("name", "%"))
                .andExpect(jsonPath("$[*].title", contains("Descuento 50% gamer")));
        mvc.perform(get("/api/events").param("name", "_"))
                .andExpect(jsonPath("$[*].title", contains("Torneo_relampago")));
        mvc.perform(get("/api/events").param("name", "\\"))
                .andExpect(jsonPath("$[*].title", empty()));
    }

    @Test
    void cityFilterIgnoresAccentsInMySql() throws Exception {
        // Diferencia real con H2: la intercalación por defecto de MySQL (utf8mb4_0900_ai_ci) ignora tildes.
        save("Indie Play", "Valparaíso", "Inscripción", LocalDateTime.of(2026, 12, 5, 11, 0));
        mvc.perform(get("/api/events").param("city", "Valparaiso"))
                .andExpect(jsonPath("$[*].title", contains("Indie Play")));
    }

    @Test
    void userStoredInMySqlCanLogInThroughTheApi() throws Exception {
        RegistrationForm form = new RegistrationForm();
        form.setName("Usuario MySQL");
        form.setEmail("mysql@example.test");
        form.setPassword("ClaveSegura2026!");
        form.setConfirmPassword("ClaveSegura2026!");
        users.register(form);
        entityManager.flush();
        entityManager.clear();
        mvc.perform(post(Constants.LOGIN_URL).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"mysql@example.test\",\"password\":\"ClaveSegura2026!\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString());
    }
}
