package cl.duoc.gamerevents.repository;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import cl.duoc.gamerevents.model.EventComment;

public interface EventCommentRepository extends JpaRepository<EventComment, Long> {
    @EntityGraph(attributePaths = "user")
    Page<EventComment> findByEventId(long eventId, Pageable pageable);
}
