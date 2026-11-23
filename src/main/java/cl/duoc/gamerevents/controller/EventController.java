package cl.duoc.gamerevents.controller;

import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import cl.duoc.gamerevents.model.EventFilter;
import cl.duoc.gamerevents.model.Modality;
import cl.duoc.gamerevents.model.CommentForm;
import cl.duoc.gamerevents.model.RatingForm;
import cl.duoc.gamerevents.service.EventInteractionService;
import cl.duoc.gamerevents.service.EventMediaService;
import cl.duoc.gamerevents.service.EventShareService;
import cl.duoc.gamerevents.service.EventPublicationService;
import cl.duoc.gamerevents.service.EventService;

@Controller
@RequestMapping("/eventos")
public class EventController {
    private final EventService events;
    private final EventInteractionService interactions;
    private final EventMediaService media;
    private final EventShareService shares;
    private final EventPublicationService publication;

    public EventController(EventService events, EventInteractionService interactions, EventMediaService media,
            EventShareService shares, EventPublicationService publication) {
        this.events = events;
        this.interactions = interactions;
        this.media=media; this.shares=shares; this.publication=publication;
    }

    @GetMapping("/buscar")
    public String list(@ModelAttribute("filter") EventFilter filter, Model model) {
        model.addAttribute("events", events.search(filter));
        model.addAttribute("games", events.games());
        model.addAttribute("categories", events.categories());
        model.addAttribute("countries", events.countries());
        model.addAttribute("cities", events.cities());
        model.addAttribute("modalities", Modality.values());
        return "events";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable long id, @RequestParam(defaultValue = "0") int page, Principal principal, Model model) {
        model.addAttribute("event", events.detail(id));
        model.addAttribute("comments", interactions.comments(id, page));
        model.addAttribute("ratings", interactions.ratings(id));
        model.addAttribute("media", media.list(id));
        model.addAttribute("ownsEvent", publication.owns(id, principal == null ? null : principal.getName()));
        model.addAttribute("shareUrl", shares.url(id));
        model.addAttribute("encodedShareUrl", shares.encodedUrl(id));
        if (!model.containsAttribute("commentForm")) model.addAttribute("commentForm", new CommentForm());
        if (!model.containsAttribute("ratingForm")) model.addAttribute("ratingForm", new RatingForm());
        return "event-detail";
    }
}
