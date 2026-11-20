package cr.oficiocr.api;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.Arrays;

@Configuration
public class SecurityConfig {
    @Bean PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    @Bean SecurityFilterChain securityFilterChain(HttpSecurity http,
            @Value("${app.frontend-origin}") String origins,
            @Value("${SESSION_COOKIE_SECURE:false}") boolean secureCookies) throws Exception {
        var csrfRepo = CookieCsrfTokenRepository.withHttpOnlyFalse();
        csrfRepo.setCookieName("oficiocr.csrf");
        csrfRepo.setHeaderName("X-CSRF-TOKEN");
        csrfRepo.setCookiePath("/");
        csrfRepo.setCookieCustomizer(cookie -> cookie.httpOnly(true).sameSite("Strict").secure(secureCookies));
        http.cors(c -> {}).csrf(c -> c.csrfTokenRepository(csrfRepo))
            .authorizeHttpRequests(a -> a
                .requestMatchers("/api/health", "/api/auth/csrf", "/api/auth/register", "/api/auth/login", "/api/auth/password/**", "/error").permitAll()
                .requestMatchers("/api/users", "/api/admin/**", "/api/test/**").hasRole("superusuario")
                .requestMatchers(HttpMethod.GET, "/api/jobs").permitAll()
                .requestMatchers("/api/auth/me", "/api/jobs/**", "/api/auth/logout").authenticated()
                .anyRequest().denyAll())
            .exceptionHandling(e -> e
                .authenticationEntryPoint((req,res,ex) -> res.sendError(401))
                .accessDeniedHandler((request,response,exception) -> {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"message\":\"La solicitud CSRF no es valida o vencio. Recarga la pagina e intentalo de nuevo.\"}");
                }))
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
            .formLogin(f -> f.disable()).httpBasic(b -> b.disable()).logout(l -> l.disable());
        return http.build();
    }

    @Bean CorsConfigurationSource corsConfigurationSource(@Value("${app.frontend-origin}") String origins) {
        var config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.stream(origins.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList());
        config.setAllowedMethods(Arrays.asList("GET","POST","PUT","PATCH","DELETE","OPTIONS"));
        config.setAllowedHeaders(Arrays.asList("Content-Type","X-CSRF-TOKEN"));
        config.setAllowCredentials(true);
        var source = new UrlBasedCorsConfigurationSource(); source.registerCorsConfiguration("/**", config); return source;
    }
}