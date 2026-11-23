package cl.duoc.gamerevents.repository;

import java.util.Optional;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import cl.duoc.gamerevents.model.EventShare;

public interface EventShareRepository extends JpaRepository<EventShare,Long> {
    @EntityGraph(attributePaths={"event","event.owner"})
    Page<EventShare> findAll(Pageable pageable);
    Optional<EventShare> findByEventId(long eventId);
}
