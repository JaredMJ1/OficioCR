package cr.oficiocr.api;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.util.Locale;

@Component
public class DatabaseInitializer implements ApplicationRunner {
    private final UserRepository users; private final PasswordEncoder passwords; private final String email,password;
    public DatabaseInitializer(UserRepository users,PasswordEncoder passwords,
        @Value("${SUPERUSER_EMAIL:admin@oficiocr.local}") String email,
        @Value("${SUPERUSER_PASSWORD:AdminOficioCR!2026}") String password){this.users=users;this.passwords=passwords;this.email=email;this.password=password;}
    @Override @Transactional public void run(ApplicationArguments args){
        String normalized=email.trim().toLowerCase(Locale.ROOT);if(users.existsByEmail(normalized))return;
        var u=new UserEntity();u.name="Superusuario";u.email=normalized;u.role="superusuario";u.passwordHash=passwords.encode(password);users.save(u);
    }
}
