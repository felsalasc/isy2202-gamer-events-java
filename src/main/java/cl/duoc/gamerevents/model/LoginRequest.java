package cl.duoc.gamerevents.model;

import jakarta.validation.constraints.NotBlank;

/** Cuerpo JSON de {@code POST /api/auth/login}. */
public record LoginRequest(@NotBlank String email, @NotBlank String password) {
}
