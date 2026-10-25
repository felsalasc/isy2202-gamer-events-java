package cl.duoc.gamerevents;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import cl.duoc.gamerevents.config.DemoDataInitializer;
import cl.duoc.gamerevents.repository.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:demo-tests;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "app.demo.password=Solo-para-pruebas-2026!"
})
@ActiveProfiles("demo")
@AutoConfigureMockMvc
class DemoDataTests {
    @Autowired MockMvc mvc;
    @Autowired DemoDataInitializer initializer;
    @Autowired GamerUserRepository users;
    @Autowired EventRepository events;
    @Autowired BrandAdRepository brands;

    @Test
    void demoSeedsThreeAuthenticatableAccountsAndIsIdempotent() throws Exception {
        initializer.run(new DefaultApplicationArguments(new String[0]));
        assertThat(users.count()).isEqualTo(3);
        assertThat(events.count()).isEqualTo(3);
        assertThat(brands.count()).isEqualTo(3);
        for (int index = 1; index <= 3; index++) {
            mvc.perform(post("/login").with(csrf()).param("email", "gamer" + index + "@example.test")
                    .param("password", "Solo-para-pruebas-2026!"))
                    .andExpect(authenticated()).andExpect(redirectedUrl("/perfil"));
        }
    }
}
