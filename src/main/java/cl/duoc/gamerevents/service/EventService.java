package cl.duoc.gamerevents.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import cl.duoc.gamerevents.model.*;
import cl.duoc.gamerevents.repository.*;

@Service
@Transactional(readOnly = true)
public class EventService {
    private final EventRepository events;
    private final BrandAdRepository brands;
    public EventService(EventRepository events, BrandAdRepository brands) { this.events = events; this.brands = brands; }
    public List<GamerEvent> upcoming() { return events.findTop3ByStartsAtGreaterThanEqualOrderByStartsAtAsc(LocalDateTime.now()); }
    public List<GamerEvent> featured() { return events.findTop3ByFeaturedTrueAndStartsAtGreaterThanEqualOrderByStartsAtAsc(LocalDateTime.now()); }
    public List<BrandAd> advertisements() { return brands.findAll(Sort.by("name")); }
    public List<String> categories() { return events.categories(); }
    public List<String> locations() { return events.locations(); }
    public GamerEvent detail(long id) {
        return events.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }
    public List<GamerEvent> search(String category, LocalDate date, String location) {
        if (date != null && (date.getYear() < 1900 || date.getYear() > 2100)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        if ((category != null && category.length() > 60) || (location != null && location.length() > 100)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        return events.findAll((root, query, cb) -> {
            List<Predicate> filters = new ArrayList<>();
            if (category != null && !category.isBlank()) {
                filters.add(cb.equal(cb.lower(root.get("category")), category.strip().toLowerCase(Locale.ROOT)));
            }
            if (location != null && !location.isBlank()) {
                filters.add(cb.equal(cb.lower(root.get("location")), location.strip().toLowerCase(Locale.ROOT)));
            }
            if (date != null) {
                filters.add(cb.greaterThanOrEqualTo(root.get("startsAt"), date.atStartOfDay()));
                filters.add(cb.lessThan(root.get("startsAt"), date.plusDays(1).atStartOfDay()));
            }
            return cb.and(filters.toArray(Predicate[]::new));
        }, Sort.by("startsAt").ascending().and(Sort.by("id")));
    }
}
