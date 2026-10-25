package cl.duoc.gamerevents.service;

public class DuplicateEmailException extends RuntimeException {
    public DuplicateEmailException() { super("El correo ya está registrado."); }
}
