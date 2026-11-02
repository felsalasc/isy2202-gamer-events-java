package cl.duoc.gamerevents.config;

import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import io.jsonwebtoken.security.Keys;

/** Constantes JWT. El secreto no vive aquí: se lee de {@code JWT_SECRET}. */
public final class Constants {
    private Constants() {
    }

    // Spring Security
    public static final String LOGIN_URL = "/api/auth/login";
    public static final String HEADER_AUTHORIZACION_KEY = "Authorization";
    public static final String TOKEN_BEARER_PREFIX = "Bearer ";

    // JWT
    public static final String ISSUER_INFO = "gamer-events";
    public static final String AUTHORITIES_CLAIM = "authorities";
    public static final long TOKEN_EXPIRATION_TIME = 30 * 60 * 1000L; // 30 minutos
    public static final int MIN_SECRET_BYTES = 32; // HS256

    public static SecretKey getSigningKey(String secret) {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < MIN_SECRET_BYTES) {
            throw new IllegalStateException("JWT_SECRET debe tener al menos 32 bytes (256 bits) para HS256.");
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
