package cl.duoc.gamerevents.config;

import jakarta.servlet.DispatcherType;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
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

        /**
         * Cadena de la API: solo atiende {@code /api/**}. Sin sesión ni CSRF, porque el
         * cliente se
         * autentica en cada petición con {@code Authorization: Bearer}. Sin token
         * responde 401 en JSON
         * en vez de redirigir al formulario web.
         */
        @Bean
        @Order(1)
        SecurityFilterChain apiSecurityFilterChain(HttpSecurity http, JWTAuthorizationFilter jwtAuthorizationFilter)
                        throws Exception {
                http.securityMatcher("/api/**")
                                .authorizeHttpRequests(authorize -> authorize
                                                .requestMatchers(HttpMethod.POST, Constants.LOGIN_URL).permitAll()
                                                .requestMatchers(HttpMethod.GET, "/api/home", "/api/events").permitAll()
                                                .anyRequest().authenticated())
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .csrf(AbstractHttpConfigurer::disable)
                                .formLogin(AbstractHttpConfigurer::disable)
                                .httpBasic(AbstractHttpConfigurer::disable)
                                .logout(AbstractHttpConfigurer::disable)
                                .requestCache(AbstractHttpConfigurer::disable)
                                .exceptionHandling(exceptions -> exceptions.authenticationEntryPoint((request, response,
                                                exception) -> JWTAuthorizationFilter.writeUnauthorized(response,
                                                                "Se requiere un token Bearer.")))
                                .addFilterBefore(jwtAuthorizationFilter, UsernamePasswordAuthenticationFilter.class);
                return http.build();
        }

        /**
         * Cadena web: todo lo que no es {@code /api/**}. Formulario de login, sesión y
         * CSRF.
         */
        @Bean
        @Order(2)
        SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http.authorizeHttpRequests(authorize -> authorize
                                .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                                // "/eventos" es la búsqueda pública; "/eventos/{id}" (detalle) no calza y queda
                                // privado.
                                .requestMatchers(HttpMethod.GET, "/", "/home", "/recetas", "/eventos", "/login",
                                                "/registro", "/css/**", "/images/**")
                                .permitAll()
                                .requestMatchers(HttpMethod.POST, "/registro").permitAll()
                                .anyRequest().authenticated())
                                .formLogin(form -> form.loginPage("/login").usernameParameter("email")
                                                .defaultSuccessUrl("/perfil", true).failureUrl("/login?error")
                                                .permitAll())
                                .logout(logout -> logout.logoutUrl("/logout").logoutSuccessUrl("/login?logout"));
                return http.build();
        }

        @Bean
        FilterRegistrationBean<JWTAuthorizationFilter> jwtAuthorizationFilterRegistration(
                        JWTAuthorizationFilter filter) {
                FilterRegistrationBean<JWTAuthorizationFilter> registration = new FilterRegistrationBean<>(filter);
                registration.setEnabled(false);
                return registration;
        }

        @Bean
        PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        UserDetailsService userDetailsService(GamerUserRepository users) {
                return email -> users.findByEmail(UserService.normalize(email))
                                .map(user -> User.withUsername(user.getEmail()).password(user.getPasswordHash())
                                                .roles(user.getRole().name()).build())
                                .orElseThrow(() -> new UsernameNotFoundException("Credenciales inválidas"));
        }
}
