package cl.duoc.gamerevents.model;

import java.time.LocalDateTime;
import jakarta.persistence.*;

@Entity
@Table(name = "gamer_events")
public class GamerEvent {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 140) private String title;
    @Column(nullable = false, length = 60) private String category;
    @Column(nullable = false, length = 100) private String location;
    @Column(nullable = false, length = 200) private String address;
    @Column(nullable = false) private LocalDateTime startsAt;
    @Column(nullable = false, length = 4000) private String description;
    @Column(nullable = false, length = 1000) private String organizers;
    @Column(nullable = false, length = 2000) private String services;
    @Column(nullable = false, length = 2000) private String exhibitors;
    @Column(nullable = false) private boolean featured;

    protected GamerEvent() { }
    public GamerEvent(String title, String category, String location, String address, LocalDateTime startsAt,
                      String description, String organizers, String services, String exhibitors, boolean featured) {
        this.title = title; this.category = category; this.location = location; this.address = address;
        this.startsAt = startsAt; this.description = description; this.organizers = organizers;
        this.services = services; this.exhibitors = exhibitors; this.featured = featured;
    }
    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getCategory() { return category; }
    public String getLocation() { return location; }
    public String getAddress() { return address; }
    public LocalDateTime getStartsAt() { return startsAt; }
    public String getDescription() { return description; }
    public String getOrganizers() { return organizers; }
    public String getServices() { return services; }
    public String getExhibitors() { return exhibitors; }
    public boolean isFeatured() { return featured; }
}
