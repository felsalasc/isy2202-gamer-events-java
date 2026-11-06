package cl.duoc.gamerevents.controller;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import cl.duoc.gamerevents.model.UserSummary;
import cl.duoc.gamerevents.service.UserService;

/**
 * API de administración, solo ADMIN. Mismo servicio que {@code /admin}: la regla
 * {@code /api/admin/**} y {@code @PreAuthorize} en el servicio controlan el acceso.
 */
@RestController
public class AdminApiController {
    private final UserService users;

    public AdminApiController(UserService users) {
        this.users = users;
    }

    @GetMapping("/api/admin/users")
    public List<UserSummary> users() {
        return users.listUsers();
    }
}
