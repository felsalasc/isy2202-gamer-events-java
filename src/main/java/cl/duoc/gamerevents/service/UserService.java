package cl.duoc.gamerevents.service;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Optional;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import cl.duoc.gamerevents.model.*;
import cl.duoc.gamerevents.repository.GamerUserRepository;

@Service
@Transactional(readOnly = true)
public class UserService {
    private static final int BCRYPT_MAX_BYTES = 72;
    private final GamerUserRepository users;
    private final PasswordEncoder encoder;
    private final String dummyHash;

    public UserService(GamerUserRepository users, PasswordEncoder encoder) {
        this.users = users;
        this.encoder = encoder;
        this.dummyHash = encoder.encode("hash-de-relleno-para-tiempo-constante");
    }

    /**
     * Credenciales de la API. Vacío si el correo no existe o la contraseña no
     * coincide.
     */
    public Optional<GamerUser> authenticate(String email, String password) {
        if (password.getBytes(StandardCharsets.UTF_8).length > BCRYPT_MAX_BYTES) {
            return Optional.empty();
        }
        Optional<GamerUser> user = users.findByEmail(normalize(email));
        String hash = user.map(GamerUser::getPasswordHash).orElse(dummyHash);
        boolean matches = encoder.matches(password, hash);
        return matches ? user : Optional.empty();
    }

    public GamerUser findByEmail(String email) {
        return users.findByEmail(normalize(email))
                .orElseThrow(() -> new UsernameNotFoundException("Cuenta no disponible"));
    }

    public static String normalize(String email) {
        return email.strip().toLowerCase(Locale.ROOT);
    }

    @Transactional
    public GamerUser register(RegistrationForm form) {
        String email = normalize(form.getEmail());
        if (users.existsByEmail(email)) {
            throw new DuplicateEmailException();
        }
        return users.saveAndFlush(new GamerUser(form.getName(), email, encoder.encode(form.getPassword())));
    }

    @Transactional
    public void updateProfile(String authenticatedEmail, ProfileForm form) {
        GamerUser user = findByEmail(authenticatedEmail);
        user.updateProfile(form.getName(), form.getNickname(), form.getAvatar(), form.getGames(),
                form.isNotifications());
    }
}
