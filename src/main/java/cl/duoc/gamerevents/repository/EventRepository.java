package cl.duoc.gamerevents.repository;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.*;
import cl.duoc.gamerevents.model.GamerEvent;

public interface EventRepository extends JpaRepository<GamerEvent, Long>, JpaSpecificationExecutor<GamerEvent> {
    List<GamerEvent> findTop3ByStartsAtGreaterThanEqualOrderByStartsAtAsc(LocalDateTime now);
    List<GamerEvent> findTop3ByFeaturedTrueAndStartsAtGreaterThanEqualOrderByStartsAtAsc(LocalDateTime now);
    @Query("select distinct e.category from GamerEvent e order by e.category") List<String> categories();
    @Query("select distinct e.location from GamerEvent e order by e.location") List<String> locations();
}
