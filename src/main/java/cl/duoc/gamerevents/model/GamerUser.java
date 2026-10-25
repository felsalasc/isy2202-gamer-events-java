package cl.duoc.gamerevents.model;

import jakarta.persistence.*;

@Entity
@Table(name = "gamer_users", uniqueConstraints = @UniqueConstraint(name = "uk_gamer_email", columnNames = "email"))
public class GamerUser {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 80)
    private String name;
    @Column(nullable = false, length = 254)
    private String email;
    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;
    @Column(nullable = false, length = 40)
    private String nickname = "";
    @Column(nullable = false, length = 20)
    private String avatar = "controller";
    @Column(nullable = false, length = 200)
    private String games = "";
    @Column(nullable = false)
    private boolean notifications;

    protected GamerUser() { }
    public GamerUser(String name, String email, String passwordHash) {
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
    }
    public void updateProfile(String name, String nickname, String avatar, String games, boolean notifications) {
        this.name = name;
        this.nickname = nickname;
        this.avatar = avatar;
        this.games = games;
        this.notifications = notifications;
    }
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public String getNickname() { return nickname; }
    public String getAvatar() { return avatar; }
    public String getGames() { return games; }
    public boolean isNotifications() { return notifications; }
}
