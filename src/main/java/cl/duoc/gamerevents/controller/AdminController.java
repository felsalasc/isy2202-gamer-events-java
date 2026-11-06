package cl.duoc.gamerevents.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import cl.duoc.gamerevents.service.UserService;

/**
 * Administración de solo lectura. El acceso lo controlan la regla
 * {@code /admin/**} de la cadena web y {@code @PreAuthorize} en el servicio.
 */
@Controller
public class AdminController {
    private final UserService users;

    public AdminController(UserService users) {
        this.users = users;
    }

    @GetMapping("/admin")
    public String admin(Model model) {
        model.addAttribute("users", users.listUsers());
        return "admin";
    }
}
