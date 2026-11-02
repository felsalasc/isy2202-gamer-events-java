package cl.duoc.gamerevents.model;

import java.time.LocalDateTime;

public record EventSummary(Long id, String title, String category, String game, String country, String city,
        Modality modality, LocalDateTime startsAt, String imageUrl, boolean featured) {

    public static EventSummary from(GamerEvent event) {
        return new EventSummary(event.getId(), event.getTitle(), event.getCategory(), event.getGame(),
                event.getCountry(), event.getLocation(), event.getModality(), event.getStartsAt(),
                event.getImageUrl(), event.isFeatured());
    }
}
