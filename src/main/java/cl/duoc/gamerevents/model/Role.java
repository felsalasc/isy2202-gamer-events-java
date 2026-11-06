package cl.duoc.gamerevents.model;

/**
 * Rol del usuario. Se guarda en la BD como texto (USER o ADMIN) y Spring
 * Security lo expone como autoridad {@code ROLE_USER} o {@code ROLE_ADMIN}.
 */
public enum Role {
    USER,
    ADMIN
}
