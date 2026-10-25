package cl.duoc.gamerevents.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import cl.duoc.gamerevents.service.EventService;

@Controller
public class HomeController {
    private final EventService events;
    public HomeController(EventService events) { this.events = events; }

    @GetMapping({"/", "/home", "/recetas"})
    public String home(Model model) {
        model.addAttribute("siteName", "Gamer Events");
        model.addAttribute("upcoming", events.upcoming());
        model.addAttribute("featured", events.featured());
        model.addAttribute("brands", events.advertisements());
        return "home";
    }
}
