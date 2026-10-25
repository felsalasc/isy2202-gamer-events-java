package cl.duoc.gamerevents;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
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
    @ValueSource(strings = {"/perfil", "/eventos", "/eventos/1", "/h2-console"})
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
}
