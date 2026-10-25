package cl.duoc.gamerevents.model;

import jakarta.persistence.*;

@Entity
@Table(name = "brand_ads")
public class BrandAd {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, length = 80) private String name;
    @Column(nullable = false, length = 200) private String message;
    protected BrandAd() { }
    public BrandAd(String name, String message) { this.name = name; this.message = message; }
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getMessage() { return message; }
}
