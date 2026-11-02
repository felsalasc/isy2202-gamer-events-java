package cl.duoc.gamerevents.model;

/** Respuesta del login: el token se envía después como {@code Authorization: Bearer <token>}. */
public record LoginResponse(String token) {
}
