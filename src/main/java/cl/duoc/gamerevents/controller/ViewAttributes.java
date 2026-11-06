package cl.duoc.gamerevents.controller;

import java.security.Principal;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class ViewAttributes {
    @ModelAttribute("signedIn")
    public boolean signedIn(Principal principal) { return principal != null; }

    /**
     * Solo decide si se muestra el enlace del menú. No es un control de acceso:
     * {@code /admin} lo protegen la regla de la cadena web y {@code @PreAuthorize}.
     */
    @ModelAttribute("isAdmin")
    public boolean isAdmin(Authentication authentication) {
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
    }
}
