package cl.duoc.gamerevents.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import static cl.duoc.gamerevents.config.Constants.*;

/**
 * Valida el encabezado {@code Authorization: Bearer} en las rutas
 * {@code /api/**}.
 */
@Component
public class JWTAuthorizationFilter extends OncePerRequestFilter {
    private final JWTAuthenticationConfig jwtAuthenticationConfig;

    public JWTAuthorizationFilter(JWTAuthenticationConfig jwtAuthenticationConfig) {
        this.jwtAuthenticationConfig = jwtAuthenticationConfig;
    }

    private Claims setSigningKey(HttpServletRequest request) {
        String jwtToken = request.getHeader(HEADER_AUTHORIZACION_KEY).substring(TOKEN_BEARER_PREFIX.length()).strip();
        return Jwts.parser()
                .verifyWith(jwtAuthenticationConfig.getSigningKey())
                .requireIssuer(ISSUER_INFO)
                .build()
                .parseSignedClaims(jwtToken)
                .getPayload();
    }

    private void setAuthentication(Claims claims) {
        List<?> authorities = claims.get(AUTHORITIES_CLAIM, List.class);
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(claims.getSubject(), null,
                authorities.stream().map(String::valueOf).map(SimpleGrantedAuthority::new).toList());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private boolean isJWTValid(HttpServletRequest request) {
        String authenticationHeader = request.getHeader(HEADER_AUTHORIZACION_KEY);
        return authenticationHeader != null && authenticationHeader.startsWith(TOKEN_BEARER_PREFIX);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            if (isJWTValid(request)) {
                Claims claims = setSigningKey(request);
                if (claims.getSubject() != null && claims.get(AUTHORITIES_CLAIM) instanceof List) {
                    setAuthentication(claims);
                } else {
                    SecurityContextHolder.clearContext();
                }
            } else {
                SecurityContextHolder.clearContext();
            }
        } catch (JwtException | IllegalArgumentException exception) {
            // Firma alterada, token expirado, malformado o de otro emisor: 401 sin detallar
            // la causa.
            SecurityContextHolder.clearContext();
            writeUnauthorized(response, "Token inválido o expirado.");
            return;
        }
        filterChain.doFilter(request, response);
    }

    /** Respuesta 401 común de la API: no se sabe quién es. Incluye {@code WWW-Authenticate: Bearer}. */
    public static void writeUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setHeader("WWW-Authenticate", "Bearer");
        writeJsonError(response, HttpServletResponse.SC_UNAUTHORIZED, message);
    }

    /** Respuesta 403 común de la API: se sabe quién es, pero su rol no tiene permiso. */
    public static void writeForbidden(HttpServletResponse response, String message) throws IOException {
        writeJsonError(response, HttpServletResponse.SC_FORBIDDEN, message);
    }

    private static void writeJsonError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write("{\"error\":\"" + message + "\"}");
    }
}
