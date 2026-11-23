package cl.duoc.gamerevents;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;
import cl.duoc.gamerevents.repository.GamerUserRepository;
import cl.duoc.gamerevents.service.UserService;

/** Defensa contra la enumeración de usuarios por tiempo de respuesta (OWASP A07). */
class UserServiceTests {
    private final GamerUserRepository users = mock(GamerUserRepository.class);
    private final PasswordEncoder encoder = mock(PasswordEncoder.class);

    /** Correo inexistente: igual se ejecuta BCrypt, para que la respuesta tarde lo mismo. */
    @Test
    void unknownEmailStillComparesAgainstDummyHash() {
        when(encoder.encode(any())).thenReturn("hash-de-relleno");
        when(users.findByEmail(anyString())).thenReturn(Optional.empty());

        var result = new UserService(users, encoder).authenticate("nadie@example.test", "Clave-cualquiera-1");

        assertThat(result).isEmpty();
        verify(encoder).matches("Clave-cualquiera-1", "hash-de-relleno");
    }

    /** Hallazgo SAST "Hardcoded Password": el relleno no debe salir de un texto fijo en el código. */
    @Test
    void dummyHashSeedIsRandomPerInstance() {
        new UserService(users, encoder);
        new UserService(users, encoder);

        ArgumentCaptor<CharSequence> seeds = ArgumentCaptor.forClass(CharSequence.class);
        verify(encoder, times(2)).encode(seeds.capture());
        assertThat(seeds.getAllValues().get(0)).isNotEqualTo(seeds.getAllValues().get(1));
    }
}
