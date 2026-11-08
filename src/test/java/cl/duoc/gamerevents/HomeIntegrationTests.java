package cl.duoc.gamerevents;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class HomeIntegrationTests {

    @Autowired
    private MockMvc mvc;

    @ParameterizedTest
    @ValueSource(strings = {"/", "/home", "/recetas"})
    void homeIsPublicAndRenderedByThymeleaf(String path) throws Exception {
        mvc.perform(get(path))
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(model().attribute("siteName", "Gamer Events"))
                .andExpect(content().string(containsString("<title>Gamer Events")))
                .andExpect(content().string(containsString("/css/styles.css")))
                .andExpect(content().string(containsString("Todavía no hay eventos publicados")));
    }

    @Test
    void stylesheetIsPublic() throws Exception {
        mvc.perform(get("/css/styles.css"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/css"));
    }

    @ParameterizedTest
    // "/eventos" salió de esta lista en la Etapa 2: la pauta define la búsqueda como pública.
    @ValueSource(strings = {"/perfil", "/eventos/1", "/h2-console"})
    void unimplementedOrPrivateRoutesAreNotPublic(String path) throws Exception {
        mvc.perform(get(path)).andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void csrfProtectionRejectsPostWithoutToken() throws Exception {
        mvc.perform(post("/recetas")).andExpect(status().isForbidden());
    }

    @Test
    void securityHeadersArePresentOnHome() throws Exception {
        mvc.perform(get("/recetas"))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().string("X-Frame-Options", "DENY"));
    }

    // H01 (ZAP 10038): las páginas HTML que reportó ZAP deben llevar una CSP estricta.
    // Sin 'unsafe-inline': la app no tiene scripts ni estilos en línea, y permitirlos anularía la defensa contra XSS.
    @ParameterizedTest
    @ValueSource(strings = {"/recetas", "/login", "/registro", "/eventos"})
    void htmlPagesSendStrictContentSecurityPolicy(String path) throws Exception {
        mvc.perform(get(path))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Security-Policy", containsString("default-src 'self'")))
                .andExpect(header().string("Content-Security-Policy", containsString("object-src 'none'")))
                .andExpect(header().string("Content-Security-Policy", containsString("base-uri 'self'")))
                .andExpect(header().string("Content-Security-Policy", containsString("form-action 'self'")))
                .andExpect(header().string("Content-Security-Policy", containsString("frame-ancestors 'none'")))
                .andExpect(header().string("Content-Security-Policy", not(containsString("unsafe-inline"))));
    }
}
