package cl.duoc.gamerevents.controller;

import java.security.Principal;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import cl.duoc.gamerevents.model.*;
import cl.duoc.gamerevents.service.EventInteractionService;

@RestController
@RequestMapping("/api/events/{id}")
public class EventInteractionApiController {
    private final EventInteractionService interactions;
    public EventInteractionApiController(EventInteractionService interactions) { this.interactions=interactions; }
    @GetMapping("/comments") public CommentPage comments(@PathVariable long id, @RequestParam(defaultValue="0") int page) {
        return interactions.comments(id,page);
    }
    @PostMapping("/comments") @ResponseStatus(HttpStatus.CREATED)
    public CommentResponse comment(@PathVariable long id, Principal principal, @Valid @RequestBody CommentForm form) {
        return interactions.comment(id, principal.getName(), form.getText());
    }
    @GetMapping("/ratings") public RatingSummary ratings(@PathVariable long id) { return interactions.ratings(id); }
    @PutMapping("/ratings/me") public RatingSummary rate(@PathVariable long id, Principal principal, @Valid @RequestBody RatingForm form) {
        return interactions.rate(id, principal.getName(), form.getScore().intValueExact());
    }
}
