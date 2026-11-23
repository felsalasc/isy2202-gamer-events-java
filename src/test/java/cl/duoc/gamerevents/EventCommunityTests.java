package cl.duoc.gamerevents;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import javax.imageio.ImageIO;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import cl.duoc.gamerevents.config.JWTAuthenticationConfig;
import cl.duoc.gamerevents.model.*;
import cl.duoc.gamerevents.repository.GamerUserRepository;
import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class EventCommunityTests {
    static final String OWNER="owner@example.test", OTHER="other@example.test";
    @Autowired MockMvc mvc;
    @Autowired GamerUserRepository users;
    @Autowired JWTAuthenticationConfig jwt;
    @BeforeEach void users() {
        users.save(new GamerUser("Owner", OWNER, "unused"));
        users.save(new GamerUser("Other", OTHER, "unused"));
    }
    String token(String email) { return "Bearer "+jwt.getJWTToken(email,Role.USER); }
    long publish() throws Exception {
        String result=mvc.perform(post("/api/events").header("Authorization",token(OWNER))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Mi encuentro\",\"game\":\"Valorant\",\"category\":\"Esports\",\"country\":\"Chile\",\"modality\":\"ONLINE\",\"accessUrl\":\"https://example.test/join\",\"startsAt\":\""+java.time.LocalDateTime.now().plusDays(10).withNano(0)+"\",\"description\":\"Encuentro de la comunidad\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return ((Number)JsonPath.read(result,"$.id")).longValue();
    }
    byte[] png() throws Exception {
        var output=new ByteArrayOutputStream(); ImageIO.write(new BufferedImage(2,2,BufferedImage.TYPE_INT_RGB),"png",output);
        return output.toByteArray();
    }
    @Test void ownerUploadsPhotoAndOtherUsersCanViewPrivately() throws Exception {
        long id=publish();
        String response=mvc.perform(multipart("/api/events/"+id+"/media")
                .file(new MockMultipartFile("file","../../foto.png","image/png",png()))
                .header("Authorization",token(OWNER))).andExpect(status().isCreated())
                .andExpect(jsonPath("$.contentType").value("image/png"))
                .andReturn().getResponse().getContentAsString();
        long media=((Number)JsonPath.read(response,"$.id")).longValue();
        mvc.perform(get("/api/events/"+id+"/media/"+media).header("Authorization",token(OTHER)))
                .andExpect(status().isOk()).andExpect(content().contentType("image/png"));
        mvc.perform(get("/api/events/"+id+"/media/"+media)).andExpect(status().isUnauthorized());
        mvc.perform(get("/eventos/"+id+"/multimedia/"+media).with(user(OTHER)))
                .andExpect(status().isOk()).andExpect(content().contentType("image/png"));
    }
    @Test void onlyOwnerCanUploadAndShare() throws Exception {
        long id=publish();
        mvc.perform(multipart("/api/events/"+id+"/media").file(new MockMultipartFile("file","p.png","image/png",png()))
                .header("Authorization",token(OTHER))).andExpect(status().isForbidden());
        mvc.perform(post("/api/events/"+id+"/shares").header("Authorization",token(OTHER)))
                .andExpect(status().isForbidden());
    }
    @Test void fakeFilesAndOversizedFilesAreRejected() throws Exception {
        long id=publish();
        for (MockMultipartFile file:new MockMultipartFile[]{
                new MockMultipartFile("file","x.png","image/png","<script>alert(1)</script>".getBytes()),
                new MockMultipartFile("file","x.svg","image/svg+xml","<svg/>".getBytes()),
                new MockMultipartFile("file","x.mp4","video/mp4","bad video".getBytes()),
                new MockMultipartFile("file","x.png","image/png",new byte[5*1024*1024+1])})
            mvc.perform(multipart("/api/events/"+id+"/media").file(file).header("Authorization",token(OWNER)))
                    .andExpect(status().is4xxClientError());
    }
    @Test void shareIsIdempotentAndCommunityLinksNeverContainTokens() throws Exception {
        long id=publish();
        for(int i=0;i<2;i++) mvc.perform(post("/api/events/"+id+"/shares").header("Authorization",token(OWNER)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.eventId").value(id))
                .andExpect(jsonPath("$.url").value("http://localhost/eventos/"+id));
        mvc.perform(get("/api/community").header("Authorization",token(OTHER)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.items",hasSize(1)))
                .andExpect(jsonPath("$.items[0].title").value("Mi encuentro"))
                .andExpect(content().string(not(containsString("accessUrl"))))
                .andExpect(content().string(not(containsString("Bearer"))));
        mvc.perform(get("/comunidad").with(user(OTHER))).andExpect(status().isOk())
                .andExpect(content().string(containsString("Mi encuentro")));
    }
    @Test void webUploadsAndSharesRequireCsrf() throws Exception {
        long id=publish();
        mvc.perform(multipart("/eventos/"+id+"/multimedia").file(new MockMultipartFile("file","p.png","image/png",png()))
                .with(user(OWNER))).andExpect(status().isForbidden());
        mvc.perform(post("/eventos/"+id+"/compartir").with(user(OWNER))).andExpect(status().isForbidden());
        mvc.perform(post("/eventos/"+id+"/compartir").with(user(OWNER)).with(csrf()))
                .andExpect(status().is3xxRedirection());
    }
    @Test void invalidEventCannotBePublished() throws Exception {
        mvc.perform(post("/api/events").header("Authorization",token(OWNER)).contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"\",\"accessUrl\":\"javascript:alert(1)\"}"))
                .andExpect(status().isBadRequest());
    }
    @Test void publishingPageIsPrivate() throws Exception {
        mvc.perform(get("/eventos/nuevo")).andExpect(status().is3xxRedirection());
        mvc.perform(get("/eventos/nuevo").with(user(OWNER))).andExpect(status().isOk())
                .andExpect(view().name("event-new"));
    }
    @Test void videoCanBeReadInRangesAndCannotBeReadThroughAnotherEvent() throws Exception {
        long id=publish();
        byte[] video;
        try(var input=getClass().getResourceAsStream("/sample.mp4")) { video=input.readAllBytes(); }
        String response=mvc.perform(multipart("/api/events/"+id+"/media")
                .file(new MockMultipartFile("file","clip.mp4","video/mp4",video)).header("Authorization",token(OWNER)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        long media=((Number)JsonPath.read(response,"$.id")).longValue();
        mvc.perform(get("/api/events/"+id+"/media/"+media).header("Authorization",token(OTHER)).header("Range","bytes=0-15"))
                .andExpect(status().isPartialContent()).andExpect(header().string("Content-Length","16"))
                .andExpect(content().bytes(java.util.Arrays.copyOf(video,16)));
        mvc.perform(get("/api/events/"+id+"/media/"+media).header("Authorization",token(OTHER)).header("Range","bytes=999999-"))
                .andExpect(status().isRequestedRangeNotSatisfiable());
        long otherEvent=publish();
        mvc.perform(get("/api/events/"+otherEvent+"/media/"+media).header("Authorization",token(OTHER)))
                .andExpect(status().isNotFound());
    }
    @Test void webCanPublishAndUploadWithCsrf() throws Exception {
        mvc.perform(post("/eventos/nuevo").with(user(OWNER)).with(csrf())
                .param("title","Evento presencial").param("game","Valorant").param("category","Esports")
                .param("country","Chile").param("modality","PRESENCIAL").param("city","Santiago")
                .param("address","Av. de prueba 123").param("startsAt",java.time.LocalDateTime.now().plusDays(10).withSecond(0).withNano(0).toString())
                .param("description","Descripción de prueba"))
                .andExpect(status().is3xxRedirection());
        long id=publish();
        mvc.perform(multipart("/eventos/"+id+"/multimedia").file(new MockMultipartFile("file","p.png","image/png",png()))
                .with(user(OWNER)).with(csrf())).andExpect(status().is3xxRedirection());
        mvc.perform(get("/eventos/"+id).with(user(OWNER))).andExpect(status().isOk())
                .andExpect(content().string(containsString("Compartir en WhatsApp")))
                .andExpect(content().string(containsString("/multimedia/")));
        mvc.perform(get("/eventos/"+id).with(user(OTHER))).andExpect(status().isOk())
                .andExpect(content().string(not(containsString("Comparte tu evento"))));
    }
}
