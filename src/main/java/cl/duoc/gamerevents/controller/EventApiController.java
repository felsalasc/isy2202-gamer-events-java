package cl.duoc.gamerevents.controller;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import cl.duoc.gamerevents.model.BrandAdResponse;
import cl.duoc.gamerevents.model.EventDetail;
import cl.duoc.gamerevents.model.EventFilter;
import cl.duoc.gamerevents.model.EventSummary;
import cl.duoc.gamerevents.model.HomeResponse;
import cl.duoc.gamerevents.service.EventService;

@RestController
@RequestMapping("/api")
public class EventApiController {
    private final EventService events;

    public EventApiController(EventService events) {
        this.events = events;
    }

    @GetMapping("/home")
    public HomeResponse home() {
        return new HomeResponse(
                events.upcoming().stream().map(EventSummary::from).toList(),
                events.featured().stream().map(EventSummary::from).toList(),
                events.advertisements().stream().map(BrandAdResponse::from).toList());
    }

    @GetMapping("/events")
    public List<EventSummary> search(@ModelAttribute EventFilter filter) {
        return events.search(filter).stream().map(EventSummary::from).toList();
    }

    @GetMapping("/events/{id}")
    public EventDetail detail(@PathVariable long id) {
        return EventDetail.from(events.detail(id));
    }
}
