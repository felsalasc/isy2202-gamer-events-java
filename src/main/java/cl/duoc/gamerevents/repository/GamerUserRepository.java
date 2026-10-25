package cl.duoc.gamerevents.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import cl.duoc.gamerevents.model.GamerUser;

public interface GamerUserRepository extends JpaRepository<GamerUser, Long> {
    Optional<GamerUser> findByEmail(String email);
    boolean existsByEmail(String email);
}
