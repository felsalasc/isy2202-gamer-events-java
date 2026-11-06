package cl.duoc.gamerevents;

import java.util.List;
import java.util.UUID;
import com.jayway.jsonpath.JsonPath;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import cl.duoc.gamerevents.config.Constants;
import cl.duoc.gamerevents.config.JWTAuthenticationConfig;
import cl.duoc.gamerevents.model.GamerUser;
import cl.duoc.gamerevents.model.Role;
import cl.duoc.gamerevents.repository.GamerUserRepository;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Fase 2 (A01): el rol guardado en la BD es el que usa Spring Security. */
@SpringBootTest
@AutoConfigureMockMvc
class AdminRoleTests {
    private static final String PASSWORD = "Pruebas-locales-2026!";

    @Autowired MockMvc mvc;
    @Autowired GamerUserRepository users;
    @Autowired PasswordEncoder encoder;
    @Autowired JWTAuthenticationConfig jwt;

    private String createUser(Role role) {
        String email = role.name().toLowerCase() + "-" + UUID.randomUUID() + "@example.test";
        users.save(new GamerUser("Usuario " + role, email, encoder.encode(PASSWORD), role));
        return email;
    }

    @Test
    void webLoginGivesAdminTheAdminRole() throws Exception {
        String email = createUser(Role.ADMIN);
        mvc.perform(post("/login").with(csrf()).param("email", email).param("password", PASSWORD))
                .andExpect(authenticated().withRoles("ADMIN"));
    }

    @Test
    void webLoginGivesUserOnlyTheUserRole() throws Exception {
        String email = createUser(Role.USER);
        mvc.perform(post("/login").with(csrf()).param("email", email).param("password", PASSWORD))
                .andExpect(authenticated().withRoles("USER"));
    }

    @ParameterizedTest
    @EnumSource(Role.class)
    void apiLoginTokenCarriesTheRealRole(Role role) throws Exception {
        String email = createUser(role);
        String response = mvc.perform(post(Constants.LOGIN_URL).contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\",\"password\":\"" + PASSWORD + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String token = JsonPath.read(response, "$.token");
        Claims claims = Jwts.parser().verifyWith(jwt.getSigningKey()).build().parseSignedClaims(token).getPayload();
        assertThat(claims.get(Constants.AUTHORITIES_CLAIM, List.class)).containsExactly("ROLE_" + role.name());
    }
}
