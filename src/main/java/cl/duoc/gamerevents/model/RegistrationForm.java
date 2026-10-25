package cl.duoc.gamerevents.model;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import jakarta.validation.constraints.*;

public class RegistrationForm {
    @NotBlank(message = "Ingresa tu nombre.")
    @Size(min = 2, max = 80, message = "El nombre debe tener entre 2 y 80 caracteres.")
    private String name = "";
    @NotBlank(message = "Ingresa tu correo.")
    @Email(message = "Ingresa un correo válido.")
    @Size(max = 254, message = "El correo es demasiado largo.")
    private String email = "";
    @NotBlank(message = "Ingresa una contraseña.")
    @Size(min = 12, max = 72, message = "Usa entre 12 y 72 caracteres (máximo 72 bytes).")
    private String password = "";
    private String confirmPassword = "";

    @AssertTrue(message = "Las contraseñas deben coincidir.")
    public boolean isPasswordConfirmed() { return password != null && password.equals(confirmPassword); }
    @AssertTrue(message = "La contraseña supera el máximo de 72 bytes de BCrypt.")
    public boolean isPasswordWithinByteLimit() {
        return password == null || password.getBytes(StandardCharsets.UTF_8).length <= 72;
    }
    public String getName() { return name; }
    public void setName(String name) { this.name = name == null ? "" : name.strip(); }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email == null ? "" : email.strip().toLowerCase(Locale.ROOT); }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getConfirmPassword() { return confirmPassword; }
    public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }
}
