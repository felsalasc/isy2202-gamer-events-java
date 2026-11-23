package cl.duoc.gamerevents.model;

import java.time.LocalDateTime;
import jakarta.persistence.*;

@Entity
@Table(name="event_shares",uniqueConstraints=@UniqueConstraint(name="uk_share_event",columnNames="event_id"))
public class EventShare {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="event_id") private GamerEvent event;
    @Column(nullable=false) private LocalDateTime createdAt;
    protected EventShare() { }
    public EventShare(GamerEvent event) { this.event=event; this.createdAt=LocalDateTime.now(); }
    public GamerEvent getEvent() { return event; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
