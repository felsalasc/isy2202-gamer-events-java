package cl.duoc.gamerevents.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.web.server.ResponseStatusException;
import cl.duoc.gamerevents.model.*;
import cl.duoc.gamerevents.repository.*;

@Service
@Transactional(readOnly = true)
public class EventInteractionService {
    private final EventService events;
    private final UserService users;
    private final EventCommentRepository comments;
    private final EventRatingRepository ratings;
    private final EntityManager entityManager;
    public EventInteractionService(EventService events, UserService users, EventCommentRepository comments,
            EventRatingRepository ratings, EntityManager entityManager) {
        this.events=events; this.users=users; this.comments=comments; this.ratings=ratings; this.entityManager=entityManager;
    }
    public CommentPage comments(long eventId, int page) {
        events.detail(eventId);
        if (page < 0 || page > 100000) throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        var result = comments.findByEventId(eventId, PageRequest.of(page, 20,
                Sort.by(Sort.Direction.DESC, "createdAt", "id")));
        return new CommentPage(result.getContent().stream().map(CommentResponse::from).toList(), page,
                result.getTotalElements(), result.hasNext());
    }
    @Transactional
    public CommentResponse comment(long eventId, String email, String text) {
        if (text == null || text.isBlank() || text.strip().length() > 1000)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        return CommentResponse.from(comments.save(new EventComment(events.detail(eventId), users.findByEmail(email), text.strip())));
    }
    public RatingSummary ratings(long eventId) { events.detail(eventId); return ratings.summarize(eventId); }
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public RatingSummary rate(long eventId, String email, int score) {
        if (score < 1 || score > 5) throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        GamerEvent event = events.detail(eventId);
        GamerUser user = users.findByEmail(email);
        // Serializa inserciones del mismo usuario incluso cuando aún no existe una valoración.
        entityManager.lock(user, LockModeType.PESSIMISTIC_WRITE);
        var rating = ratings.findByEventIdAndUserId(eventId, user.getId())
                .orElseGet(() -> new EventRating(event, user, score));
        rating.setScore(score);
        ratings.saveAndFlush(rating);
        return ratings.summarize(eventId);
    }
}
