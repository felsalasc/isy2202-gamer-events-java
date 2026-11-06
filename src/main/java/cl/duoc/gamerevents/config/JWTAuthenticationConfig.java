package cl.duoc.gamerevents.config;

import java.util.Date;
import java.util.List;
import javax.crypto.SecretKey;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import cl.duoc.gamerevents.model.Role;
import static cl.duoc.gamerevents.config.Constants.*;

/** Genera los tokens de la API. La aplicación no arranca si {@code JWT_SECRET} falta o es corto. */
@Configuration
public class JWTAuthenticationConfig {
    private final SecretKey signingKey;

    public JWTAuthenticationConfig(@Value("${app.jwt.secret:}") String secret) {
        this.signingKey = Constants.getSigningKey(secret);
    }

    public SecretKey getSigningKey() { return signingKey; }

    /** El claim {@code authorities} lleva el rol real del usuario: {@code ROLE_USER} o {@code ROLE_ADMIN}. */
    public String getJWTToken(String username, Role role) {
        List<String> authorities = List.of("ROLE_" + role.name());
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .subject(username)
                .issuer(ISSUER_INFO)
                .claim(AUTHORITIES_CLAIM, authorities)
                .issuedAt(new Date(now))
                .expiration(new Date(now + TOKEN_EXPIRATION_TIME))
                .signWith(signingKey, Jwts.SIG.HS256)
                .compact();
    }
}
