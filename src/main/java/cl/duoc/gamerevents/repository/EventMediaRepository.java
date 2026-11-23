package cl.duoc.gamerevents.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.*;
import cl.duoc.gamerevents.model.*;

public interface EventMediaRepository extends JpaRepository<EventMedia,Long> {
    @Query("select new cl.duoc.gamerevents.model.MediaSummary(m.id,m.contentType) from EventMedia m where m.event.id=:eventId order by m.id")
    List<MediaSummary> summaries(long eventId);
    Optional<EventMedia> findByIdAndEventId(long id,long eventId);
    long countByEventId(long eventId);
}
