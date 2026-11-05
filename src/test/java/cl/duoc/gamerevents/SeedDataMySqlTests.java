package cl.duoc.gamerevents;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mysql.MySQLContainer;
import org.testcontainers.utility.MountableFile;
import cl.duoc.gamerevents.model.GamerEvent;
import cl.duoc.gamerevents.model.GamerUser;
import cl.duoc.gamerevents.model.Modality;
import cl.duoc.gamerevents.model.Role;
import cl.duoc.gamerevents.repository.BrandAdRepository;
import cl.duoc.gamerevents.repository.EventRepository;
import cl.duoc.gamerevents.repository.GamerUserRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Los scripts de db/ sobre un MySQL 8.4 real, cargados igual que en docker-compose.yml
 * (/docker-entrypoint-initdb.d). La aplicación arranca con ddl-auto=validate: si el esquema
 * del script no calza con las entidades, el contexto no levanta y todas las pruebas fallan.
 * Si Docker no está disponible, se omiten.
 */
@SpringBootTest(properties = "spring.jpa.hibernate.ddl-auto=validate")
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
class SeedDataMySqlTests {

    @Container
    @ServiceConnection
    static MySQLContainer mysql = new MySQLContainer("mysql:8.4")
            .withCopyFileToContainer(MountableFile.forHostPath("db"), "/docker-entrypoint-initdb.d");

    @Autowired
    MockMvc mvc;
    @Autowired
    GamerUserRepository users;
    @Autowired
    EventRepository events;
    @Autowired
    BrandAdRepository brands;

    @Test
    void seedsThreeUsersAndOneAdminWithBcryptHashes() {
        Map<String, GamerUser> byEmail = users.findAll().stream()
                .collect(Collectors.toMap(GamerUser::getEmail, user -> user));

        assertThat(byEmail).containsOnlyKeys("gamer1@example.test", "gamer2@example.test",
                "gamer3@example.test", "admin@example.test");
        assertThat(byEmail.get("admin@example.test").getRole()).isEqualTo(Role.ADMIN);
        for (int index = 1; index <= 3; index++) {
            assertThat(byEmail.get("gamer" + index + "@example.test").getRole()).isEqualTo(Role.USER);
        }
        // Solo hashes BCrypt en la BD; los USER comparten contraseña y el ADMIN tiene otra.
        assertThat(byEmail.values()).allSatisfy(user -> assertThat(user.getPasswordHash()).startsWith("$2a$10$"));
        String userHash = byEmail.get("gamer1@example.test").getPasswordHash();
        assertThat(byEmail.get("gamer3@example.test").getPasswordHash()).isEqualTo(userHash);
        assertThat(byEmail.get("admin@example.test").getPasswordHash()).isNotEqualTo(userHash);
    }

    @Test
    void seededAccountsRejectAWrongPassword() throws Exception {
        mvc.perform(post("/login").with(csrf()).param("email", "admin@example.test")
                .param("password", "no-es-la-contrasena-2026"))
                .andExpect(unauthenticated());
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"gamer1@example.test\",\"password\":\"no-es-la-contrasena-2026\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void seededEventsCoverEveryFilterAndHaveRealImages() throws Exception {
        List<GamerEvent> all = events.findAll();

        assertThat(all).hasSize(6);
        assertThat(brands.count()).isEqualTo(3);
        assertThat(all).extracting(GamerEvent::getModality).contains(Modality.PRESENCIAL, Modality.ONLINE);
        assertThat(all.stream().map(GamerEvent::getCountry).distinct()).hasSizeGreaterThan(1);
        assertThat(all).extracting(GamerEvent::getGame).doesNotHaveDuplicates();
        for (GamerEvent event : all) {
            mvc.perform(get(event.getImageUrl())).andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith("image/svg+xml"));
        }
    }

    @Test
    void seedKeepsAccentsAndLineBreaks() {
        GamerEvent valparaiso = events.findAll().stream()
                .filter(event -> "Valparaíso".equals(event.getLocation())).findFirst().orElseThrow();

        assertThat(valparaiso.getTitle()).isEqualTo("Indie Play · Mundos por descubrir");
        assertThat(valparaiso.getServices()).contains("\nLocomoción colectiva:");
    }
}
