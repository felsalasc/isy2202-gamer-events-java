package cl.duoc.gamerevents;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.*;
import org.testcontainers.mysql.MySQLContainer;
import org.testcontainers.utility.MountableFile;
import org.springframework.http.MediaType;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Ejecuta las mismas operaciones comunitarias con el esquema SQL real, sin generación JPA. */
@SpringBootTest(properties="spring.jpa.hibernate.ddl-auto=validate")
@Testcontainers(disabledWithoutDocker=true)
class WeekFiveMySqlTests extends EventCommunityTests {
    @Container @ServiceConnection
    static MySQLContainer mysql=new MySQLContainer("mysql:8.4")
            .withCopyFileToContainer(MountableFile.forHostPath("db"),"/docker-entrypoint-initdb.d");

    @Test void sqlSchemaSupportsCommentsRatingsAndEmoji() throws Exception {
        long id=publish();
        mvc.perform(post("/api/events/"+id+"/comments").header("Authorization",token(OWNER))
                .contentType(MediaType.APPLICATION_JSON).content("{\"text\":\"Vamos 🎮\"}"))
                .andExpect(status().isCreated());
        mvc.perform(get("/api/events/"+id+"/comments").header("Authorization",token(OTHER)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.items[0].text").value("Vamos 🎮"));
        for(int score:new int[]{5,1}) mvc.perform(put("/api/events/"+id+"/ratings/me").header("Authorization",token(OWNER))
                .contentType(MediaType.APPLICATION_JSON).content("{\"score\":"+score+"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.count").value(1))
                .andExpect(jsonPath("$.average").value(score));
    }
}
