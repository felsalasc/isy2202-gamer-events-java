package cl.duoc.gamerevents;

import java.util.List;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import cl.duoc.gamerevents.config.Constants;
import cl.duoc.gamerevents.config.JWTAuthenticationConfig;
import cl.duoc.gamerevents.model.Role;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

class JwtTokenTests {
    private static final String SECRET = "secreto-solo-para-pruebas-automatizadas-2026";

    @ParameterizedTest
    @ValueSource(strings = {"", "secreto-de-solo-31-bytes-123456"})
    void missingOrShortSecretPreventsStartup(String secret) {
        assertThatIllegalStateException().isThrownBy(() -> new JWTAuthenticationConfig(secret))
                .withMessageContaining("JWT_SECRET");
    }

    @ParameterizedTest
    @EnumSource(Role.class)
    void tokenIsSignedWithHs256AndCarriesSubjectIssuerAuthoritiesAndExpiration(Role role) {
        JWTAuthenticationConfig config = new JWTAuthenticationConfig(SECRET);
        String token = config.getJWTToken("gamer@example.test", role);
        var jws = Jwts.parser().verifyWith(config.getSigningKey()).build().parseSignedClaims(token);
        Claims claims = jws.getPayload();
        assertThat(jws.getHeader().getAlgorithm()).isEqualTo("HS256");
        assertThat(claims.getSubject()).isEqualTo("gamer@example.test");
        assertThat(claims.getIssuer()).isEqualTo(Constants.ISSUER_INFO);
        assertThat(claims.get(Constants.AUTHORITIES_CLAIM, List.class)).containsExactly("ROLE_" + role.name());
        assertThat(claims.getExpiration().getTime() - claims.getIssuedAt().getTime())
                .isEqualTo(Constants.TOKEN_EXPIRATION_TIME);
    }
}
