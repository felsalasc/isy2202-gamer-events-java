package cl.duoc.gamerevents.service;

import java.util.Locale;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import cl.duoc.gamerevents.model.*;
import cl.duoc.gamerevents.repository.GamerUserRepository;

@Service
@Transactional(readOnly = true)
public class UserService {
    private final GamerUserRepository users;
    private final PasswordEncoder encoder;
    public UserService(GamerUserRepository users, PasswordEncoder encoder) {
        this.users = users; this.encoder = encoder;
    }
    public GamerUser findByEmail(String email) {
        return users.findByEmail(normalize(email))
                .orElseThrow(() -> new UsernameNotFoundException("Cuenta no disponible"));
    }
    public static String normalize(String email) { return email.strip().toLowerCase(Locale.ROOT); }
    @Transactional
    public GamerUser register(RegistrationForm form) {
        String email = normalize(form.getEmail());
        if (users.existsByEmail(email)) { throw new DuplicateEmailException(); }
        return users.saveAndFlush(new GamerUser(form.getName(), email, encoder.encode(form.getPassword())));
    }
    @Transactional
    public void updateProfile(String authenticatedEmail, ProfileForm form) {
        GamerUser user = findByEmail(authenticatedEmail);
        user.updateProfile(form.getName(), form.getNickname(), form.getAvatar(), form.getGames(), form.isNotifications());
    }
}
