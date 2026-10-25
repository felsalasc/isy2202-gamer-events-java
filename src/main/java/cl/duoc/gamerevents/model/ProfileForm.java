package cl.duoc.gamerevents.model;

import jakarta.validation.constraints.*;

public class ProfileForm {
    @NotBlank(message = "Ingresa tu nombre.")
    @Size(min = 2, max = 80, message = "Usa entre 2 y 80 caracteres.")
    private String name = "";
    @Size(max = 40, message = "El alias admite hasta 40 caracteres.")
    private String nickname = "";
    @NotNull
    @Pattern(regexp = "controller|robot|rocket", message = "Selecciona un avatar disponible.")
    private String avatar = "controller";
    @Size(max = 200, message = "Los juegos admiten hasta 200 caracteres.")
    private String games = "";
    private boolean notifications;

    public static ProfileForm from(GamerUser user) {
        ProfileForm form = new ProfileForm();
        form.setName(user.getName()); form.setNickname(user.getNickname());
        form.setAvatar(user.getAvatar()); form.setGames(user.getGames());
        form.setNotifications(user.isNotifications());
        return form;
    }
    public String getName() { return name; }
    public void setName(String name) { this.name = name == null ? "" : name.strip(); }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname == null ? "" : nickname.strip(); }
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    public String getGames() { return games; }
    public void setGames(String games) { this.games = games == null ? "" : games.strip(); }
    public boolean isNotifications() { return notifications; }
    public void setNotifications(boolean notifications) { this.notifications = notifications; }
}
