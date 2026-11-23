package cl.duoc.gamerevents.model;

import java.time.LocalDateTime;
import jakarta.persistence.*;

@Entity
@Table(name = "event_comments", indexes = @Index(name = "ix_comment_event_date", columnList = "event_id,created_at,id"))
public class EventComment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "event_id") private GamerEvent event;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "user_id") private GamerUser user;
    @Column(nullable = false, length = 1000) private String text;
    @Column(nullable = false) private LocalDateTime createdAt;
    protected EventComment() { }
    public EventComment(GamerEvent event, GamerUser user, String text) {
        this.event = event; this.user = user; this.text = text; this.createdAt = LocalDateTime.now();
    }
    public Long getId() { return id; }
    public GamerUser getUser() { return user; }
    public String getText() { return text; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
