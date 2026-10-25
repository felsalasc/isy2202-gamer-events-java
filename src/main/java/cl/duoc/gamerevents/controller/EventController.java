package cl.duoc.gamerevents.controller;

import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import cl.duoc.gamerevents.service.EventService;

@Controller
@RequestMapping("/eventos")
public class EventController {
    private final EventService events;
    public EventController(EventService events) { this.events = events; }
    @GetMapping
    public String list(@RequestParam(required = false) String category,
                       @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                       @RequestParam(required = false) String location, Model model) {
        model.addAttribute("events", events.search(category, date, location));
        model.addAttribute("categories", events.categories()); model.addAttribute("locations", events.locations());
        model.addAttribute("category", category); model.addAttribute("date", date); model.addAttribute("location", location);
        return "events";
    }
    @GetMapping("/{id}")
    public String detail(@PathVariable long id, Model model) {
        model.addAttribute("event", events.detail(id));
        return "event-detail";
    }
}
