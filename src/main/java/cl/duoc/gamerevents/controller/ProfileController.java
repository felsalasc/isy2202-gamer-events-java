package cl.duoc.gamerevents.controller;

import java.security.Principal;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import cl.duoc.gamerevents.model.ProfileForm;
import cl.duoc.gamerevents.service.UserService;

@Controller
public class ProfileController {
    private final UserService users;
    public ProfileController(UserService users) { this.users = users; }
    @GetMapping("/perfil")
    public String profile(Principal principal, Model model) {
        model.addAttribute("profile", ProfileForm.from(users.findByEmail(principal.getName())));
        return "profile";
    }
    @PostMapping("/perfil")
    public String update(Principal principal, @Valid @ModelAttribute("profile") ProfileForm form, BindingResult errors) {
        if (errors.hasErrors()) { return "profile"; }
        users.updateProfile(principal.getName(), form);
        return "redirect:/perfil?guardado";
    }
}
