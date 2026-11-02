package cl.duoc.gamerevents;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import cl.duoc.gamerevents.model.GamerEvent;
import cl.duoc.gamerevents.model.Modality;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

/** Reglas del evento presencial u online. Prueba unitaria: no levanta Spring ni la BD. */
class GamerEventTests {
    private static final LocalDateTime STARTS_AT = LocalDateTime.of(2026, 12, 5, 18, 30);

    private static GamerEvent presencial(String city, String address) {
        return GamerEvent.presencial("Torneo", "Esports", "Valorant", "Chile", city, address, STARTS_AT,
                "Descripción", "Organizador", "Servicios", "Expositores", "Mayores de 16 años",
                "Inscripción gratuita en el recinto", "/images/events/nexus-arena.svg", false);
    }

    private static GamerEvent online(String accessUrl) {
        return GamerEvent.online("Liga", "Esports", "Rocket League", "México", accessUrl, STARTS_AT,
                "Descripción", "Organizador", "Servicios", "Expositores", "Equipos de 3 jugadores",
                "Inscripción previa por formulario", "/images/events/liga-rocket.svg", true);
    }

    @Test
    void presencialEventHasCityAndAddressButNoAccessUrl() {
        GamerEvent event = presencial("Santiago", "Avenida Virtual 120");
        assertThat(event.getModality()).isEqualTo(Modality.PRESENCIAL);
        assertThat(event.isOnline()).isFalse();
        assertThat(event.getLocation()).isEqualTo("Santiago");
        assertThat(event.getAddress()).isEqualTo("Avenida Virtual 120");
        assertThat(event.getAccessUrl()).isNull();
        assertThat(event.getGame()).isEqualTo("Valorant");
        assertThat(event.getCountry()).isEqualTo("Chile");
        assertThat(event.getRequirements()).isEqualTo("Mayores de 16 años");
        assertThat(event.getRegistrationInfo()).isEqualTo("Inscripción gratuita en el recinto");
        assertThat(event.getImageUrl()).isEqualTo("/images/events/nexus-arena.svg");
    }

    @Test
    void onlineEventHasAccessUrlButNoCityOrAddress() {
        GamerEvent event = online("https://eventos.example.test/liga");
        assertThat(event.getModality()).isEqualTo(Modality.ONLINE);
        assertThat(event.isOnline()).isTrue();
        assertThat(event.getAccessUrl()).isEqualTo("https://eventos.example.test/liga");
        assertThat(event.getLocation()).isNull();
        assertThat(event.getAddress()).isNull();
    }

    @Test
    void presencialEventRequiresCityAndAddress() {
        assertThatIllegalArgumentException().isThrownBy(() -> presencial(" ", "Avenida Virtual 120"))
                .withMessageContaining("ciudad");
        assertThatIllegalArgumentException().isThrownBy(() -> presencial("Santiago", null))
                .withMessageContaining("dirección");
    }

    @Test
    void onlineEventRequiresHttpsAccessUrl() {
        assertThatIllegalArgumentException().isThrownBy(() -> online(null)).withMessageContaining("enlace");
        assertThatIllegalArgumentException().isThrownBy(() -> online("http://inseguro.example.test"))
                .withMessageContaining("https");
        assertThatIllegalArgumentException().isThrownBy(() -> online("javascript:alert(1)"))
                .withMessageContaining("https");
    }

    @Test
    void commonFieldsAreRequiredInBothModalities() {
        assertThatIllegalArgumentException().isThrownBy(() -> GamerEvent.online("Liga", "Esports", "", "México",
                "https://eventos.example.test/liga", STARTS_AT, "Descripción", "Organizador", "Servicios",
                "Expositores", "Requisitos", "Inscripción", "/images/events/liga-rocket.svg", false))
                .withMessageContaining("videojuego");
        assertThatIllegalArgumentException().isThrownBy(() -> GamerEvent.presencial("Torneo", "Esports", "Valorant",
                "Chile", "Santiago", "Avenida Virtual 120", null, "Descripción", "Organizador", "Servicios",
                "Expositores", "Requisitos", "Inscripción", "/images/events/nexus-arena.svg", false))
                .withMessageContaining("fecha");
    }
}
