package cl.duoc.gamerevents.controller;

import java.security.Principal;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import cl.duoc.gamerevents.model.*;
import cl.duoc.gamerevents.service.EventInteractionService;

@Controller
@RequestMapping("/eventos/{id}")
public class EventInteractionController {
    private final EventInteractionService interactions;
    private final EventController eventController;
    public EventInteractionController(EventInteractionService interactions, EventController eventController) {
        this.interactions=interactions; this.eventController=eventController;
    }
    @PostMapping("/comentarios")
    public String comment(@PathVariable long id, Principal principal, @Valid @ModelAttribute("commentForm") CommentForm form,
            BindingResult errors, Model model) {
        if (errors.hasErrors()) return eventController.detail(id, 0, principal, model);
        interactions.comment(id, principal.getName(), form.getText());
        return "redirect:/eventos/"+id+"#comentarios";
    }
    @PostMapping("/valoracion")
    public String rate(@PathVariable long id, Principal principal, @Valid @ModelAttribute("ratingForm") RatingForm form,
            BindingResult errors, Model model) {
        if (errors.hasErrors()) return eventController.detail(id, 0, principal, model);
        interactions.rate(id, principal.getName(), form.getScore().intValueExact());
        return "redirect:/eventos/"+id+"#valoraciones";
    }
}
