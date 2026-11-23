package cl.duoc.gamerevents.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.*;
import cl.duoc.gamerevents.model.*;

public interface EventRatingRepository extends JpaRepository<EventRating, Long> {
    Optional<EventRating> findByEventIdAndUserId(long eventId, long userId);
    @Query("select new cl.duoc.gamerevents.model.RatingSummary(count(r), coalesce(avg(r.score), 0.0)) from EventRating r where r.event.id = :eventId")
    RatingSummary summarize(long eventId);
}
