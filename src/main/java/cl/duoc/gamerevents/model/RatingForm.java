package cl.duoc.gamerevents.model;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class RatingForm {
    @NotNull(message = "Selecciona una puntuación.")
    @Min(value = 1, message = "La puntuación debe estar entre 1 y 5.")
    @Max(value = 5, message = "La puntuación debe estar entre 1 y 5.")
    @Digits(integer = 1, fraction = 0, message = "Selecciona un número entero de 1 a 5.")
    private BigDecimal score;
    public BigDecimal getScore() { return score; }
    public void setScore(BigDecimal score) { this.score = score; }
}
