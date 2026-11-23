package cl.duoc.gamerevents.model;

import jakarta.persistence.*;

@Entity
@Table(name = "event_ratings", uniqueConstraints = @UniqueConstraint(name = "uk_rating_event_user", columnNames = {"event_id", "user_id"}))
public class EventRating {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "event_id") private GamerEvent event;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "user_id") private GamerUser user;
    @Column(nullable = false) private int score;
    protected EventRating() { }
    public EventRating(GamerEvent event, GamerUser user, int score) { this.event=event; this.user=user; this.score=score; }
    public void setScore(int score) { this.score=score; }
}
