package cl.duoc.gamerevents.controller;

import java.security.Principal;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class ViewAttributes {
    @ModelAttribute("signedIn")
    public boolean signedIn(Principal principal) { return principal != null; }
}
