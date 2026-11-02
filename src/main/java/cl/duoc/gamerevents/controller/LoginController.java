package cl.duoc.gamerevents.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import cl.duoc.gamerevents.config.Constants;
import cl.duoc.gamerevents.config.JWTAuthenticationConfig;
import cl.duoc.gamerevents.exception.InvalidCredentialsException;
import cl.duoc.gamerevents.model.GamerUser;
import cl.duoc.gamerevents.model.LoginRequest;
import cl.duoc.gamerevents.model.LoginResponse;
import cl.duoc.gamerevents.service.UserService;

/**
 * API de login: valida las credenciales contra la BD y entrega un token JWT.
 * Si las credenciales no son válidas, {@code ApiExceptionHandler} responde 401.
 */
@RestController
public class LoginController {
    private final UserService users;
    private final JWTAuthenticationConfig jwtAuthenticationConfig;

    public LoginController(UserService users, JWTAuthenticationConfig jwtAuthenticationConfig) {
        this.users = users;
        this.jwtAuthenticationConfig = jwtAuthenticationConfig;
    }

    @PostMapping(Constants.LOGIN_URL)
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        GamerUser user = users.authenticate(request.email(), request.password())
                .orElseThrow(InvalidCredentialsException::new);
        return new LoginResponse(jwtAuthenticationConfig.getJWTToken(user.getEmail()));
    }
}
