package cr.oficiocr.api;

import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import java.util.Optional;
import java.util.UUID;

/** Access to the signed-in user from any backend service or controller. */
@Component
public class CurrentUser {
    public Optional<AuthController.Principal> optional() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthController.Principal principal)) {
            return Optional.empty();
        }
        return Optional.of(principal);
    }

    public AuthController.Principal required() {
        return optional().orElseThrow(() -> new AuthenticationCredentialsNotFoundException("No hay un usuario autenticado."));
    }

    public UUID id() { return required().id(); }
    public String name() { return required().name(); }
    public String email() { return required().email(); }
    public String role() { return required().role(); }
}
