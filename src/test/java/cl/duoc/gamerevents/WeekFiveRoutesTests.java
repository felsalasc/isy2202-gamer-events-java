package cl.duoc.gamerevents;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class WeekFiveRoutesTests {
    @Autowired MockMvc mvc;
    @Test void deliveryUrlLoadsHomeAndCatalogRemainsPublic() throws Exception {
        mvc.perform(get("/eventos")).andExpect(status().isOk()).andExpect(view().name("home"));
        mvc.perform(get("/eventos/buscar")).andExpect(status().isOk()).andExpect(view().name("events"));
    }
}
