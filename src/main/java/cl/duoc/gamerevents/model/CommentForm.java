package cl.duoc.gamerevents.model;

import jakarta.validation.constraints.*;

public class CommentForm {
    @NotBlank(message = "Escribe un comentario.")
    @Size(max = 1000, message = "El comentario admite hasta 1000 caracteres.")
    private String text;
    public String getText() { return text; }
    public void setText(String text) { this.text = text == null ? null : text.strip(); }
}
