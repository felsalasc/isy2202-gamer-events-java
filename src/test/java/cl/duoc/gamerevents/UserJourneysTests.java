package cl.duoc.gamerevents;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class UserJourneysTests {
    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;
    @Autowired PasswordEncoder encoder;
    private static final String PASSWORD = "Pruebas-locales-2026!";

    private String register() throws Exception {
        String email = UUID.randomUUID() + "@example.test";
        mvc.perform(post("/registro").with(csrf()).param("name", "Gamer de prueba")
                .param("email", email).param("password", PASSWORD).param("confirmPassword", PASSWORD))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/login?registrado"));
        return email;
    }

    @ParameterizedTest
    @ValueSource(strings = {"/login", "/registro"})
    void authenticationPagesArePublicAndContainCsrf(String path) throws Exception {
        mvc.perform(get(path)).andExpect(status().isOk())
                .andExpect(content().string(containsString("name=\"_csrf\"")));
    }

    @Test
    void threeRegisteredUsersAuthenticateWithBcrypt() throws Exception {
        for (int i = 0; i < 3; i++) {
            String email = register();
            String hash = jdbc.queryForObject("select password_hash from gamer_users where email = ?", String.class, email);
            assertThat(hash).isNotEqualTo(PASSWORD).startsWith("$2");
            assertThat(encoder.matches(PASSWORD, hash)).isTrue();
            mvc.perform(post("/login").with(csrf()).param("email", email.toUpperCase())
                    .param("password", PASSWORD)).andExpect(authenticated())
                    .andExpect(redirectedUrl("/perfil"));
            mvc.perform(post("/login").with(csrf()).param("email", email)
                    .param("password", "incorrecta")).andExpect(unauthenticated())
                    .andExpect(redirectedUrl("/login?error"));
        }
    }

    @Test
    void duplicateEmailIsRejectedAfterNormalization() throws Exception {
        String email = register();
        mvc.perform(post("/registro").with(csrf()).param("name", "Otro gamer")
                .param("email", " " + email.toUpperCase() + " ")
                .param("password", PASSWORD).param("confirmPassword", PASSWORD))
                .andExpect(status().isOk()).andExpect(model().attributeHasFieldErrors("registration", "email"));
        assertThat(jdbc.queryForObject("select count(*) from gamer_users where email = ?", Integer.class, email)).isEqualTo(1);
    }

    @Test
    void invalidRegistrationDoesNotExposePassword() throws Exception {
        mvc.perform(post("/registro").with(csrf()).param("name", " ").param("email", "no-es-correo")
                .param("password", "secreto").param("confirmPassword", "otra"))
                .andExpect(status().isOk()).andExpect(model().attributeHasErrors("registration"))
                .andExpect(content().string(org.hamcrest.Matchers.not(containsString("value=\"secreto\""))));
    }

    @Test
    void profileUpdatesOnlyTheAuthenticatedUserAndLogoutInvalidatesSession() throws Exception {
        String email = register();
        String other = register();
        MockHttpSession session = (MockHttpSession) mvc.perform(post("/login").with(csrf())
                .param("email", email).param("password", PASSWORD)).andExpect(authenticated())
                .andReturn().getRequest().getSession(false);
        mvc.perform(post("/perfil").session(session).with(csrf()).param("name", "Nuevo nombre")
                .param("nickname", "Player Uno").param("avatar", "robot").param("games", "Minecraft, Valorant")
                .param("notifications", "true").param("email", other).param("id", "999"))
                .andExpect(redirectedUrl("/perfil?guardado"));
        assertThat(jdbc.queryForObject("select name from gamer_users where email = ?", String.class, email)).isEqualTo("Nuevo nombre");
        assertThat(jdbc.queryForObject("select name from gamer_users where email = ?", String.class, other)).isEqualTo("Gamer de prueba");
        mvc.perform(get("/perfil").session(session)).andExpect(status().isOk())
                .andExpect(content().string(containsString("Player Uno")));
        mvc.perform(post("/logout").session(session)).andExpect(status().isForbidden());
        mvc.perform(post("/logout").session(session).with(csrf())).andExpect(redirectedUrl("/login?logout"));
        assertThat(session.isInvalid()).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"/registro", "/login", "/perfil", "/logout"})
    void mutationRequiresCsrf(String path) throws Exception {
        mvc.perform(post(path)).andExpect(status().isForbidden());
    }

    @Test
    void bcryptByteLimitRejectsLongUnicodePassword() throws Exception {
        String unicodePassword = "🎮".repeat(20);
        mvc.perform(post("/registro").with(csrf()).param("name", "Player Unicode")
                .param("email", UUID.randomUUID() + "@example.test")
                .param("password", unicodePassword).param("confirmPassword", unicodePassword))
                .andExpect(status().isOk()).andExpect(model().attributeHasFieldErrors("registration", "passwordWithinByteLimit"));
    }

    @Test
    void avatarOutsideAllowlistIsRejectedAndProfileHtmlIsEscaped() throws Exception {
        String email = register();
        mvc.perform(post("/perfil").with(user(email)).with(csrf()).param("name", "Nombre seguro")
                .param("avatar", "../../otro"))
                .andExpect(status().isOk()).andExpect(model().attributeHasFieldErrors("profile", "avatar"));
        assertThat(jdbc.queryForObject("select name from gamer_users where email = ?", String.class, email)).isEqualTo("Gamer de prueba");
        mvc.perform(post("/perfil").with(user(email)).with(csrf()).param("name", "<script>test</script>")
                .param("avatar", "rocket")).andExpect(redirectedUrl("/perfil?guardado"));
        mvc.perform(get("/perfil").with(user(email))).andExpect(status().isOk())
                .andExpect(content().string(containsString("&lt;script&gt;test&lt;/script&gt;")))
                .andExpect(content().string(org.hamcrest.Matchers.not(containsString("<script>test</script>"))));
    }
}
