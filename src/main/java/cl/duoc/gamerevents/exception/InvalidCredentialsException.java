package cl.duoc.gamerevents.exception;

/** Correo inexistente o contraseña incorrecta en el login de la API. Se responde con 401. */
public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException() {
        super("Credenciales inválidas.");
    }
}
