package cl.duoc.gamerevents.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import cl.duoc.gamerevents.exception.EventNotFoundException;
import cl.duoc.gamerevents.model.*;
import cl.duoc.gamerevents.repository.*;

@Service
@Transactional(readOnly = true)
public class EventService {
    private final EventRepository events;
    private final BrandAdRepository brands;

    public EventService(EventRepository events, BrandAdRepository brands) {
        this.events = events;
        this.brands = brands;
    }

    public List<GamerEvent> upcoming() {
        return events.findTop3ByStartsAtGreaterThanEqualOrderByStartsAtAsc(LocalDateTime.now());
    }

    public List<GamerEvent> featured() {
        return events.findTop3ByFeaturedTrueAndStartsAtGreaterThanEqualOrderByStartsAtAsc(LocalDateTime.now());
    }

    public List<BrandAd> advertisements() {
        return brands.findAll(Sort.by("name"));
    }

    public List<String> categories() {
        return events.categories();
    }

    public List<String> games() {
        return events.games();
    }

    public List<String> countries() {
        return events.countries();
    }

    public List<String> cities() {
        return events.cities();
    }

    public GamerEvent detail(long id) {
        return events.findById(id).orElseThrow(EventNotFoundException::new);
    }

    /** Búsqueda pública: aplica solo los filtros con valor, combinados con AND. */
    public List<GamerEvent> search(EventFilter filter) {
        validate(filter);
        return events.findAll((root, query, cb) -> {
            List<Predicate> filters = new ArrayList<>();
            if (hasText(filter.name())) {
                String pattern = "%" + escapeLike(filter.name().strip().toLowerCase(Locale.ROOT)) + "%";
                filters.add(cb.like(cb.lower(root.get("title")), pattern, '\\'));
            }
            equalsIgnoringCase(filters, cb, root.get("game"), filter.game());
            equalsIgnoringCase(filters, cb, root.get("category"), filter.category());
            equalsIgnoringCase(filters, cb, root.get("country"), filter.country());
            equalsIgnoringCase(filters, cb, root.get("location"), filter.city());
            if (filter.date() != null) {
                filters.add(cb.greaterThanOrEqualTo(root.get("startsAt"), filter.date().atStartOfDay()));
                filters.add(cb.lessThan(root.get("startsAt"), filter.date().plusDays(1).atStartOfDay()));
            }
            if (filter.modality() != null) {
                filters.add(cb.equal(root.get("modality"), filter.modality()));
            }
            return cb.and(filters.toArray(Predicate[]::new));
        }, Sort.by("startsAt").ascending().and(Sort.by("id")));
    }

    private static void validate(EventFilter filter) {
        LocalDate date = filter.date();
        boolean invalid = (date != null && (date.getYear() < 1900 || date.getYear() > 2100))
                || tooLong(filter.name(), 140) || tooLong(filter.game(), 80) || tooLong(filter.category(), 60)
                || tooLong(filter.country(), 60) || tooLong(filter.city(), 100);
        if (invalid) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
    }

    private static void equalsIgnoringCase(List<Predicate> filters, CriteriaBuilder cb, Path<String> field,
            String value) {
        if (hasText(value)) {
            filters.add(cb.equal(cb.lower(field), value.strip().toLowerCase(Locale.ROOT)));
        }
    }

    private static String escapeLike(String text) {
        return text.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private static boolean tooLong(String value, int max) {
        return value != null && value.length() > max;
    }
}
