package cl.duoc.gamerevents.controller;

import java.security.Principal;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class ViewAttributes {
    private final Environment environment;
    public ViewAttributes(Environment environment) { this.environment = environment; }
    @ModelAttribute("signedIn")
    public boolean signedIn(Principal principal) { return principal != null; }
    @ModelAttribute("demoMode")
    public boolean demoMode() { return environment.acceptsProfiles(Profiles.of("demo")); }
}
