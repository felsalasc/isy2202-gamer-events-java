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
import cl.duoc.gamerevents.model.UserSummary;
import cl.duoc.gamerevents.repository.GamerUserRepository;
import cl.duoc.gamerevents.service.UserService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.test.context.support.WithMockUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/** Fase 2 (A01): el rol guardado en la BD es el que usa Spring Security. */
@SpringBootTest
@AutoConfigureMockMvc
class AdminRoleTests {
    private static final String PASSWORD = "Pruebas-locales-2026!";

    @Autowired MockMvc mvc;
    @Autowired GamerUserRepository users;
    @Autowired PasswordEncoder encoder;
    @Autowired JWTAuthenticationConfig jwt;
    @Autowired UserService userService;

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

    // --- 2.3 Capa 1: regla de URL en la cadena web ---

    @Test
    void anonymousIsRedirectedToLoginFromAdminPage() throws Exception {
        mvc.perform(get("/admin"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void userGetsForbiddenOnAdminPage() throws Exception {
        mvc.perform(get("/admin").with(user("gamer@example.test").roles("USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminSeesUserListWithoutPasswordHashes() throws Exception {
        String email = createUser(Role.USER);
        String hash = users.findByEmail(email).orElseThrow().getPasswordHash();
        mvc.perform(get("/admin").with(user("admin@example.test").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(view().name("admin"))
                .andExpect(content().string(containsString(email)))
                .andExpect(content().string(not(containsString(hash))));
    }

    // --- 2.3 Capa 2: @PreAuthorize en el servicio, sin pasar por ninguna URL ---

    @Test
    @WithMockUser(roles = "USER")
    void serviceRejectsUserEvenWithoutUrlRule() {
        assertThatThrownBy(() -> userService.listUsers()).isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void serviceRejectsCallsWithoutAuthentication() {
        assertThatThrownBy(() -> userService.listUsers())
                .isInstanceOf(AuthenticationCredentialsNotFoundException.class);
    }

    // --- 2.4 API GET /api/admin/users: 401 sin token, 403 USER, 200 ADMIN ---

    private static final String ADMIN_API = "/api/admin/users";

    @Test
    void adminApiWithoutTokenGets401Json() throws Exception {
        mvc.perform(get(ADMIN_API))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    void adminApiWithUserTokenGets403Json() throws Exception {
        mvc.perform(get(ADMIN_API).header("Authorization", "Bearer " + jwt.getJWTToken("gamer@example.test", Role.USER)))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value(containsString("permiso")));
    }

    @Test
    void adminApiWithAdminTokenListsUsersWithoutPasswordHashes() throws Exception {
        String email = createUser(Role.USER);
        String hash = users.findByEmail(email).orElseThrow().getPasswordHash();
        mvc.perform(get(ADMIN_API).header("Authorization", "Bearer " + jwt.getJWTToken("admin@example.test", Role.ADMIN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.email == '" + email + "')].role").value(hasItem("USER")))
                .andExpect(jsonPath("$[0].passwordHash").doesNotExist())
                .andExpect(content().string(not(containsString(hash))));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void serviceListsUsersForAdmin() {
        String email = createUser(Role.USER);
        assertThat(userService.listUsers()).extracting(UserSummary::email).contains(email);
    }
}
