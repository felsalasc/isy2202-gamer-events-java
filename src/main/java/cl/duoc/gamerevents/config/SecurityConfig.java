package cl.duoc.gamerevents.config;

import jakarta.servlet.DispatcherType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import cl.duoc.gamerevents.repository.GamerUserRepository;
import cl.duoc.gamerevents.service.UserService;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(authorize -> authorize
                        .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                        .requestMatchers(HttpMethod.GET, "/", "/home", "/recetas", "/login", "/registro", "/css/**", "/images/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/registro").permitAll()
                        .anyRequest().authenticated())
                .formLogin(form -> form.loginPage("/login").usernameParameter("email")
                        .defaultSuccessUrl("/perfil", true).failureUrl("/login?error").permitAll())
                .logout(logout -> logout.logoutUrl("/logout").logoutSuccessUrl("/login?logout"));
        // CSRF remains enabled for registration, login, profile updates and POST logout.
        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    UserDetailsService userDetailsService(GamerUserRepository users) {
        return email -> users.findByEmail(UserService.normalize(email))
                .map(user -> User.withUsername(user.getEmail()).password(user.getPasswordHash()).roles("USER").build())
                .orElseThrow(() -> new UsernameNotFoundException("Credenciales inválidas"));
    }
}
