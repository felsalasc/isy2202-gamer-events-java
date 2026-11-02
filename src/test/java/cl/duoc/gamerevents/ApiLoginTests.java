package cl.duoc.gamerevents;

import java.util.List;
import com.jayway.jsonpath.JsonPath;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
import cl.duoc.gamerevents.config.Constants;
import cl.duoc.gamerevents.config.JWTAuthenticationConfig;
import cl.duoc.gamerevents.model.RegistrationForm;
import cl.duoc.gamerevents.service.UserService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** {@code POST /api/auth/login}: credenciales de la BD, BCrypt y token JWT. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ApiLoginTests {
    private static final String EMAIL = "login.api@example.test";
    private static final String PASSWORD = "ClaveSegura2026!";

    @Autowired
    MockMvc mvc;
    @Autowired
    UserService users;
    @Autowired
    JWTAuthenticationConfig jwt;

    @BeforeEach
    void registeredUser() {
        RegistrationForm form = new RegistrationForm();
        form.setName("Login Api");
        form.setEmail(EMAIL);
        form.setPassword(PASSWORD);
        form.setConfirmPassword(PASSWORD);
        users.register(form);
    }

    private ResultActions login(String json) throws Exception {
        return mvc.perform(post(Constants.LOGIN_URL).contentType(MediaType.APPLICATION_JSON).content(json));
    }

    private static String body(String email, String password) {
        return "{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}";
    }

    @Test
    void validCredentialsReturnSignedTokenForThatUser() throws Exception {
        String response = login(body(EMAIL, PASSWORD))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").isString())
                .andReturn().getResponse().getContentAsString();

        String token = JsonPath.read(response, "$.token");
        Claims claims = Jwts.parser().verifyWith(jwt.getSigningKey()).build().parseSignedClaims(token).getPayload();
        assertThat(claims.getSubject()).isEqualTo(EMAIL);
        assertThat(claims.getIssuer()).isEqualTo(Constants.ISSUER_INFO);
        assertThat(claims.get(Constants.AUTHORITIES_CLAIM, List.class)).containsExactly("ROLE_USER");
    }

    @Test
    void emailIsNormalizedLikeWebLogin() throws Exception {
        login(body("  LOGIN.Api@Example.TEST ", PASSWORD)).andExpect(status().isOk());
    }

    @Test
    void wrongPasswordGets401() throws Exception {
        login(body(EMAIL, "ClaveIncorrecta2026!"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("WWW-Authenticate", "Bearer"))
                .andExpect(jsonPath("$.error").value("Credenciales inválidas."))
                .andExpect(jsonPath("$.token").doesNotExist());
    }

    @Test
    void unknownUserGetsSameResponseAsWrongPassword() throws Exception {
        String unknown = login(body("nadie@example.test", PASSWORD))
                .andExpect(status().isUnauthorized()).andReturn().getResponse().getContentAsString();
        String wrongPassword = login(body(EMAIL, "ClaveIncorrecta2026!"))
                .andReturn().getResponse().getContentAsString();
        assertThat(unknown).isEqualTo(wrongPassword);
    }

    @Test
    void passwordLongerThanBcryptLimitGets401NotServerError() throws Exception {
        login(body(EMAIL, "x".repeat(73))).andExpect(status().isUnauthorized());
    }

    @Test
    void blankFieldsGet400() throws Exception {
        login("{}").andExpect(status().isBadRequest());
        login(body("", "")).andExpect(status().isBadRequest());
    }

    @Test
    void missingBodyGets400() throws Exception {
        mvc.perform(post(Constants.LOGIN_URL).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void tokenFromLoginOpensPrivateApi() throws Exception {
        String response = login(body(EMAIL, PASSWORD)).andReturn().getResponse().getContentAsString();
        String token = JsonPath.read(response, "$.token");
        // Sin endpoints privados todavía: pasar la seguridad se ve como 404, no 401.
        mvc.perform(get("/api/ruta-privada-de-prueba").header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void loginOnlyAcceptsPost() throws Exception {
        mvc.perform(get(Constants.LOGIN_URL)).andExpect(status().isUnauthorized());
    }
}
