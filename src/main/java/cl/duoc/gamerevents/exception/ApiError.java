package cl.duoc.gamerevents.exception;

/** Cuerpo JSON común de los errores de la API: {@code {"error": "..."}}. */
public record ApiError(String error) {
}
