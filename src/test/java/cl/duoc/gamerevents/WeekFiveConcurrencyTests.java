package cl.duoc.gamerevents;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.testcontainers.junit.jupiter.*;
import org.testcontainers.mysql.MySQLContainer;
import org.testcontainers.utility.MountableFile;
import cl.duoc.gamerevents.model.*;
import cl.duoc.gamerevents.repository.GamerUserRepository;
import cl.duoc.gamerevents.service.*;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest(properties="spring.jpa.hibernate.ddl-auto=validate")
@Testcontainers(disabledWithoutDocker=true)
class WeekFiveConcurrencyTests {
    @Container @ServiceConnection static MySQLContainer mysql=new MySQLContainer("mysql:8.4")
            .withCopyFileToContainer(MountableFile.forHostPath("db"),"/docker-entrypoint-initdb.d");
    @Autowired GamerUserRepository users;
    @Autowired EventPublicationService publication;
    @Autowired EventInteractionService interactions;
    @Autowired EventShareService shares;
    @Autowired EventMediaService media;
    String email;
    long event() {
        email="parallel-"+UUID.randomUUID()+"@example.test";
        users.save(new GamerUser("Parallel",email,"unused"));
        return publication.publish(email,new EventPublicationForm("Concurrente","Valorant","Esports","Chile",
                Modality.ONLINE,null,null,"https://example.test/join",LocalDateTime.now().plusDays(10),"Encuentro")).getId();
    }
    void parallel(Callable<Void> operation) throws Exception {
        var pool=Executors.newFixedThreadPool(6);
        var start=new CountDownLatch(1);
        try {
            var results=new ArrayList<Future<Void>>();
            for(int i=0;i<6;i++) results.add(pool.submit(()->{ start.await(); return operation.call(); }));
            start.countDown();
            for(var result:results) result.get(30,TimeUnit.SECONDS);
        } finally { pool.shutdownNow(); }
    }
    @Test void simultaneousFirstRatingsDoNotDuplicateOrFail() throws Exception {
        long id=event();
        parallel(()->{interactions.rate(id,email,5);return null;});
        assertThat(interactions.ratings(id).count()).isEqualTo(1);
    }
    @Test void simultaneousSharesAreIdempotent() throws Exception {
        long id=event();
        parallel(()->{shares.share(id,email);return null;});
        assertThat(shares.community(0).items().stream().filter(s->s.eventId()==id).count()).isEqualTo(1);
    }
    @Test void simultaneousUploadsCannotExceedTwentyFiles() throws Exception {
        long id=event();
        var output=new ByteArrayOutputStream();ImageIO.write(new BufferedImage(1,1,BufferedImage.TYPE_INT_RGB),"png",output);
        var file=new MockMultipartFile("file","p.png","image/png",output.toByteArray());
        for(int i=0;i<19;i++) media.upload(id,email,file);
        parallel(()->{
            try {media.upload(id,email,file);}
            catch(ResponseStatusException exception) {assertThat(exception.getStatusCode().value()).isEqualTo(409);}
            return null;
        });
        assertThat(media.list(id)).hasSize(20);
    }
}
