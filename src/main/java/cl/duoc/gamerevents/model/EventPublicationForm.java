package cl.duoc.gamerevents.model;

import java.net.URI;
import java.time.LocalDateTime;
import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;

public record EventPublicationForm(
        @NotBlank @Size(max=140) String title,
        @NotBlank @Size(max=80) String game,
        @NotBlank @Size(max=60) String category,
        @NotBlank @Size(max=60) String country,
        @NotNull Modality modality,
        @Size(max=100) String city,
        @Size(max=200) String address,
        @Size(max=300) String accessUrl,
        @NotNull @Future @DateTimeFormat(iso=DateTimeFormat.ISO.DATE_TIME) LocalDateTime startsAt,
        @NotBlank @Size(max=4000) String description) {
    @AssertTrue(message="Para un evento presencial indica ciudad y dirección; para uno online indica una URL HTTPS válida.")
    public boolean isLocationValid() {
        if (modality == null) return false;
        if (modality == Modality.PRESENCIAL) return city != null && !city.isBlank() && address != null && !address.isBlank();
        try {
            URI uri=URI.create(accessUrl == null ? "" : accessUrl);
            return "https".equalsIgnoreCase(uri.getScheme()) && uri.getHost()!=null && uri.getUserInfo()==null;
        } catch (IllegalArgumentException exception) { return false; }
    }
    public static EventPublicationForm empty() {
        return new EventPublicationForm("", "", "", "Chile", Modality.PRESENCIAL, "", "", "", null, "");
    }
}
