package cl.duoc.gamerevents.model;

import java.time.LocalDateTime;

public record EventDetail(Long id, String title, String category, String game, Modality modality,
        LocalDateTime startsAt, String country, String city, String address, String accessUrl,
        String description, String organizers, String services, String exhibitors,
        String requirements, String registrationInfo, String imageUrl, boolean featured) {

    public static EventDetail from(GamerEvent event) {
        return new EventDetail(event.getId(), event.getTitle(), event.getCategory(), event.getGame(),
                event.getModality(), event.getStartsAt(), event.getCountry(), event.getLocation(),
                event.getAddress(), event.getAccessUrl(), event.getDescription(), event.getOrganizers(),
                event.getServices(), event.getExhibitors(), event.getRequirements(), event.getRegistrationInfo(),
                event.getImageUrl(), event.isFeatured());
    }
}
