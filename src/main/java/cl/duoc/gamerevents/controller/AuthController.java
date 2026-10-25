package cl.duoc.gamerevents.controller;

import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import cl.duoc.gamerevents.model.RegistrationForm;
import cl.duoc.gamerevents.service.*;

@Controller
public class AuthController {
    private final UserService users;
    public AuthController(UserService users) { this.users = users; }
    @GetMapping("/login")
    public String login() { return "login"; }
    @GetMapping("/registro")
    public String registration(Model model) {
        model.addAttribute("registration", new RegistrationForm());
        return "register";
    }
    @PostMapping("/registro")
    public String register(@Valid @ModelAttribute("registration") RegistrationForm form, BindingResult errors) {
        if (errors.hasErrors()) { return "register"; }
        try {
            users.register(form);
        } catch (DuplicateEmailException | DataIntegrityViolationException exception) {
            errors.rejectValue("email", "duplicate", "El correo ya está registrado.");
            return "register";
        }
        return "redirect:/login?registrado";
    }
}
