package cl.duoc.gamerevents;

import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import cl.duoc.gamerevents.config.JWTAuthenticationConfig;
import cl.duoc.gamerevents.model.*;
import cl.duoc.gamerevents.repository.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class EventInteractionTests {
    static final String ALICE = "alice.interaction@example.test";
    static final String BOB = "bob.interaction@example.test";
    @Autowired MockMvc mvc;
    @Autowired EventRepository events;
    @Autowired GamerUserRepository users;
    @Autowired JWTAuthenticationConfig jwt;
    long id;

    @BeforeEach void data() {
        users.save(new GamerUser("Alice", ALICE, "unused"));
        users.save(new GamerUser("Bob", BOB, "unused"));
        id = events.save(GamerEvent.online("Evento comunidad", "Esports", "Valorant", "Chile",
                "https://example.test/event", LocalDateTime.now().plusDays(2), "Descripción", "Comunidad",
                "Servicios", "Expositores", "Requisitos", "Inscripción", "/images/events/nexus-arena.svg", true)).getId();
    }
    String token(String email) { return "Bearer " + jwt.getJWTToken(email, Role.USER); }
    String api() { return "/api/events/" + id; }

    @Test void commentsUseAuthenticatedAuthorAndPersistText() throws Exception {
        mvc.perform(post(api()+"/comments").header("Authorization", token(ALICE))
                .contentType(MediaType.APPLICATION_JSON).content("{\"text\":\"  Nos vemos 🎮  \",\"userId\":999}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.author").value("Alice"))
                .andExpect(jsonPath("$.text").value("Nos vemos 🎮"))
                .andExpect(jsonPath("$.email").doesNotExist());
        mvc.perform(get(api()+"/comments").header("Authorization", token(BOB)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].author").value("Alice"));
    }
    @Test void invalidCommentsAreRejected() throws Exception {
        for (String text : new String[]{"   ", "x".repeat(1001)}) {
            mvc.perform(post(api()+"/comments").header("Authorization", token(ALICE))
                    .contentType(MediaType.APPLICATION_JSON).content("{\"text\":\""+text+"\"}"))
                    .andExpect(status().isBadRequest());
        }
    }
    @Test void ratingsArePerUserAndCanBeUpdated() throws Exception {
        rate(ALICE, "5", 200); rate(BOB, "3", 200); rate(ALICE, "1", 200);
        mvc.perform(get(api()+"/ratings").header("Authorization", token(ALICE)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.count").value(2))
                .andExpect(jsonPath("$.average").value(2.0));
    }
    @Test void invalidRatingsAreRejected() throws Exception {
        for (String value : new String[]{"0", "6", "null", "2.5"}) rate(ALICE, value, 400);
    }
    void rate(String email, String value, int status) throws Exception {
        mvc.perform(put(api()+"/ratings/me").header("Authorization", token(email))
                .contentType(MediaType.APPLICATION_JSON).content("{\"score\":"+value+"}"))
                .andExpect(status().is(status));
    }
    @Test void interactionApisRequireBearerTokenEvenWithWebSession() throws Exception {
        mvc.perform(get(api()+"/comments").with(user(ALICE))).andExpect(status().isUnauthorized());
        mvc.perform(post(api()+"/comments").contentType(MediaType.APPLICATION_JSON).content("{\"text\":\"hola\"}"))
                .andExpect(status().isUnauthorized());
        mvc.perform(put(api()+"/ratings/me").contentType(MediaType.APPLICATION_JSON).content("{\"score\":5}"))
                .andExpect(status().isUnauthorized());
    }
    @Test void missingEventsAndInvalidPagesHaveControlledErrors() throws Exception {
        mvc.perform(get("/api/events/99999999/comments").header("Authorization", token(ALICE)))
                .andExpect(status().isNotFound());
        mvc.perform(get(api()+"/comments?page=-1").header("Authorization", token(ALICE)))
                .andExpect(status().isBadRequest());
        mvc.perform(get(api()+"/comments?page=2147483647").header("Authorization", token(ALICE)))
                .andExpect(status().isBadRequest());
    }
    @Test void webFormsRequireCsrfAndEscapeComments() throws Exception {
        String path = "/eventos/"+id;
        mvc.perform(post(path+"/comentarios").with(user(ALICE)).param("text","hola"))
                .andExpect(status().isForbidden());
        mvc.perform(post(path+"/comentarios").with(user(ALICE)).with(csrf())
                .param("text","<script>alert(1)</script>"))
                .andExpect(status().is3xxRedirection());
        mvc.perform(get(path).with(user(BOB))).andExpect(status().isOk())
                .andExpect(content().string(containsString("&lt;script&gt;alert(1)&lt;/script&gt;")))
                .andExpect(content().string(not(containsString("<script>alert(1)</script>"))));
    }
    @Test void webValidationRendersTheDetailWithErrors() throws Exception {
        mvc.perform(post("/eventos/"+id+"/comentarios").with(user(ALICE)).with(csrf()).param("text", "  "))
                .andExpect(status().isOk()).andExpect(view().name("event-detail"))
                .andExpect(model().attributeHasFieldErrors("commentForm", "text"));
        mvc.perform(post("/eventos/"+id+"/valoracion").with(user(ALICE)).with(csrf()).param("score", "7"))
                .andExpect(status().isOk()).andExpect(model().attributeHasFieldErrors("ratingForm", "score"));
    }
    @Test void commentsArePaginatedAndStable() throws Exception {
        for (int i=0;i<21;i++) mvc.perform(post(api()+"/comments").header("Authorization", token(ALICE))
                .contentType(MediaType.APPLICATION_JSON).content("{\"text\":\"Comentario "+i+"\"}"))
                .andExpect(status().isCreated());
        mvc.perform(get(api()+"/comments?page=1").header("Authorization", token(ALICE)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.items",hasSize(1)))
                .andExpect(jsonPath("$.items[0].text").value("Comentario 0"))
                .andExpect(jsonPath("$.total").value(21));
    }
    @Test void sharedEventSurvivesTheRealLoginRedirect() throws Exception {
        var encoder=new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
        // Cuenta separada para recorrer el formulario real con una contraseña verificable.
        users.save(new GamerUser("Recipient", "recipient@example.test",encoder.encode("ClavePruebaSegura2026!")));
        var session=(org.springframework.mock.web.MockHttpSession)mvc.perform(get("/eventos/"+id))
                .andExpect(status().is3xxRedirection()).andReturn().getRequest().getSession(false);
        mvc.perform(post("/login").session(session).with(csrf()).param("email","recipient@example.test")
                .param("password","ClavePruebaSegura2026!"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("http://localhost/eventos/"+id+"*"));
    }
}
