package cl.duoc.gamerevents;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.test.web.servlet.MockMvc;
import cl.duoc.gamerevents.config.JWTAuthenticationConfig;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Cadena de seguridad de {@code /api/**}: stateless, JWT Bearer y 401 en JSON. */
@SpringBootTest
@AutoConfigureMockMvc
class ApiSecurityTests {
    private static final String PRIVATE_API = "/api/ruta-privada-de-prueba";

    @Autowired
    MockMvc mvc;
    @Autowired
    JWTAuthenticationConfig jwt;

    @Test
    void apiWithoutTokenGets401JsonInsteadOfLoginRedirect() throws Exception {
        mvc.perform(get(PRIVATE_API))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("WWW-Authenticate", "Bearer"))
                .andExpect(header().doesNotExist("Location"))
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(content().string(containsString("Se requiere un token Bearer")));
    }

    @Test
    void apiWithTamperedTokenGets401() throws Exception {
        String token = jwt.getJWTToken("gamer1@example.test");
        String tampered = token.substring(0, token.length() - 2) + (token.endsWith("AA") ? "BB" : "AA");
        mvc.perform(get(PRIVATE_API).header("Authorization", "Bearer " + tampered))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(containsString("Token inválido o expirado")));
    }

    @Test
    void apiWithMalformedTokenGets401() throws Exception {
        mvc.perform(get(PRIVATE_API).header("Authorization", "Bearer no-es-un-jwt"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void apiWithValidTokenPassesSecurity() throws Exception {
        // Aún no hay endpoints: superar la seguridad se ve como 404, no como 401.
        mvc.perform(get(PRIVATE_API).header("Authorization", "Bearer " + jwt.getJWTToken("gamer1@example.test")))
                .andExpect(status().isNotFound());
    }

    @Test
    void webSessionDoesNotAuthenticateApi() throws Exception {
        var auth = UsernamePasswordAuthenticationToken.authenticated("gamer1@example.test", null,
                List.of(new SimpleGrantedAuthority("ROLE_USER")));
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                new SecurityContextImpl(auth));
        mvc.perform(get(PRIVATE_API).session(session)).andExpect(status().isUnauthorized());
    }

    @Test
    void apiLoginIsPublicAndNeedsNoCsrf() throws Exception {
        // El controlador llega en el paso 2.2; aquí solo se comprueba que la seguridad no bloquea.
        int status = mvc.perform(post("/api/auth/login").contentType("application/json").content("{}"))
                .andReturn().getResponse().getStatus();
        assertThat(status).isNotIn(401, 403);
    }

    @Test
    void apiPostWithoutCsrfIsNotRejectedAsForbidden() throws Exception {
        mvc.perform(post(PRIVATE_API).header("Authorization", "Bearer " + jwt.getJWTToken("gamer1@example.test")))
                .andExpect(result -> assertThat(result.getResponse().getStatus()).isNotEqualTo(403));
    }

    @Test
    void apiDoesNotCreateSession() throws Exception {
        var result = mvc.perform(get(PRIVATE_API).header("Authorization", "Bearer " + jwt.getJWTToken("gamer1@example.test")))
                .andReturn();
        assertThat(result.getRequest().getSession(false)).isNull();
    }
}
