package cl.duoc.gamerevents.model;

/**
 * Datos de un usuario que se pueden mostrar en la administración. No incluye
 * {@code passwordHash}: el hash nunca sale del servicio hacia una vista o JSON.
 */
public record UserSummary(String name, String email, Role role) {

    public static UserSummary from(GamerUser user) {
        return new UserSummary(user.getName(), user.getEmail(), user.getRole());
    }
}
