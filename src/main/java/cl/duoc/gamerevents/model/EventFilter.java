package cl.duoc.gamerevents.model;

import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

public record EventFilter(String name, String game, String category, String country, String city,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date, Modality modality) {
}
