package cl.duoc.gamerevents.model;

import java.time.LocalDateTime;
import jakarta.persistence.*;

@Entity
@Table(name="event_media")
public class EventMedia {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="event_id") private GamerEvent event;
    @Column(nullable=false,length=30) private String contentType;
    @Lob @Column(nullable=false,columnDefinition="LONGBLOB") private byte[] content;
    @Column(nullable=false) private LocalDateTime createdAt;
    protected EventMedia() { }
    public EventMedia(GamerEvent event, String contentType, byte[] content) {
        this.event=event; this.contentType=contentType; this.content=content; this.createdAt=LocalDateTime.now();
    }
    public Long getId() { return id; }
    public String getContentType() { return contentType; }
    public byte[] getContent() { return content; }
}
