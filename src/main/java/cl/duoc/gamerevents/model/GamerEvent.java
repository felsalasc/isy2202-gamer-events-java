package cl.duoc.gamerevents.model;

import java.time.LocalDateTime;
import jakarta.persistence.*;

/**
 * Evento gamer. Se crea solo con {@link #presencial} u {@link #online}, que
 * validan las reglas de
 * cada modalidad: un evento presencial tiene ciudad y dirección; uno online, un
 * enlace https.
 */
@Entity
@Table(name = "gamer_events")
public class GamerEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 140)
    private String title;
    @Column(nullable = false, length = 60)
    private String category;
    @Column(nullable = false, length = 80)
    private String game;
    @Column(nullable = false, length = 60)
    private String country;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private Modality modality;
    @Column(length = 100)
    private String location;
    @Column(length = 200)
    private String address;
    @Column(length = 300)
    private String accessUrl;
    @Column(nullable = false)
    private LocalDateTime startsAt;
    @Column(nullable = false, length = 4000)
    private String description;
    @Column(nullable = false, length = 1000)
    private String organizers;
    @Column(nullable = false, length = 2000)
    private String services;
    @Column(nullable = false, length = 2000)
    private String exhibitors;
    @Column(nullable = false, length = 1000)
    private String requirements;
    @Column(nullable = false, length = 1000)
    private String registrationInfo;
    @Column(nullable = false, length = 200)
    private String imageUrl;
    @Column(nullable = false)
    private boolean featured;

    protected GamerEvent() {
    }

    private GamerEvent(String title, String category, String game, String country, Modality modality,
            LocalDateTime startsAt, String description, String organizers, String services,
            String exhibitors, String requirements, String registrationInfo, String imageUrl,
            boolean featured) {
        this.title = requireText(title, "título");
        this.category = requireText(category, "categoría");
        this.game = requireText(game, "videojuego");
        this.country = requireText(country, "país");
        this.modality = modality;
        if (startsAt == null) {
            throw new IllegalArgumentException("El evento requiere fecha y hora.");
        }
        this.startsAt = startsAt;
        this.description = requireText(description, "descripción");
        this.organizers = requireText(organizers, "organizador");
        this.services = requireText(services, "servicios");
        this.exhibitors = requireText(exhibitors, "expositores");
        this.requirements = requireText(requirements, "requisitos");
        this.registrationInfo = requireText(registrationInfo, "inscripción");
        this.imageUrl = requireText(imageUrl, "imagen");
        this.featured = featured;
    }

    /** Evento en un recinto físico */
    public static GamerEvent presencial(String title, String category, String game, String country, String city,
            String address, LocalDateTime startsAt, String description,
            String organizers, String services, String exhibitors,
            String requirements, String registrationInfo, String imageUrl,
            boolean featured) {
        GamerEvent event = new GamerEvent(title, category, game, country, Modality.PRESENCIAL, startsAt,
                description, organizers, services, exhibitors, requirements, registrationInfo, imageUrl, featured);
        event.location = requireText(city, "ciudad");
        event.address = requireText(address, "dirección");
        return event;
    }

    /** Evento online. */
    public static GamerEvent online(String title, String category, String game, String country, String accessUrl,
            LocalDateTime startsAt, String description, String organizers,
            String services, String exhibitors, String requirements,
            String registrationInfo, String imageUrl, boolean featured) {
        GamerEvent event = new GamerEvent(title, category, game, country, Modality.ONLINE, startsAt,
                description, organizers, services, exhibitors, requirements, registrationInfo, imageUrl, featured);
        String url = requireText(accessUrl, "enlace de acceso");
        if (!url.startsWith("https://")) {
            throw new IllegalArgumentException("El enlace de acceso debe comenzar con https://.");
        }
        event.accessUrl = url;
        return event;
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("El evento requiere " + field + ".");
        }
        return value.strip();
    }

    public boolean isOnline() {
        return modality == Modality.ONLINE;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getCategory() {
        return category;
    }

    public String getGame() {
        return game;
    }

    public String getCountry() {
        return country;
    }

    public Modality getModality() {
        return modality;
    }

    public String getLocation() {
        return location;
    }

    public String getAddress() {
        return address;
    }

    public String getAccessUrl() {
        return accessUrl;
    }

    public LocalDateTime getStartsAt() {
        return startsAt;
    }

    public String getDescription() {
        return description;
    }

    public String getOrganizers() {
        return organizers;
    }

    public String getServices() {
        return services;
    }

    public String getExhibitors() {
        return exhibitors;
    }

    public String getRequirements() {
        return requirements;
    }

    public String getRegistrationInfo() {
        return registrationInfo;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public boolean isFeatured() {
        return featured;
    }
}
