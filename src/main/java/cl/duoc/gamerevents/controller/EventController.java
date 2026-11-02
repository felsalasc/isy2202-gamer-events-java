package cl.duoc.gamerevents.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import cl.duoc.gamerevents.model.EventFilter;
import cl.duoc.gamerevents.model.Modality;
import cl.duoc.gamerevents.service.EventService;

@Controller
@RequestMapping("/eventos")
public class EventController {
    private final EventService events;

    public EventController(EventService events) {
        this.events = events;
    }

    /** [Pública] Búsqueda de eventos. Los parámetros de la URL llenan el {@link EventFilter}. */
    @GetMapping
    public String list(@ModelAttribute("filter") EventFilter filter, Model model) {
        model.addAttribute("events", events.search(filter));
        model.addAttribute("games", events.games());
        model.addAttribute("categories", events.categories());
        model.addAttribute("countries", events.countries());
        model.addAttribute("cities", events.cities());
        model.addAttribute("modalities", Modality.values());
        return "events";
    }

    /** [Privada] Detalle de eventos. */
    @GetMapping("/{id}")
    public String detail(@PathVariable long id, Model model) {
        model.addAttribute("event", events.detail(id));
        return "event-detail";
    }
}
