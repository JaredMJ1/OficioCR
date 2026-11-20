package cr.oficiocr.api;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
public class AdminController {
    private final UserRepository users; private final PasswordEncoder passwords; private final boolean testEnabled;
    public AdminController(UserRepository users,PasswordEncoder passwords,@Value("${app.test-endpoints}") boolean testEnabled){this.users=users;this.passwords=passwords;this.testEnabled=testEnabled;}
    @GetMapping("/api/users") public List<AdminUserResponse> users(){return users.findAllByOrderByCreatedAtAsc().stream().map(AdminUserResponse::new).toList();}
    @PostMapping("/api/admin/operators") public ResponseEntity<?> createOperator(@RequestBody RegisterRequest req){return create(req,"operador");}
    @GetMapping("/api/test/users") public ResponseEntity<?> testUsers(){return testEnabled?ResponseEntity.ok(users()):ResponseEntity.notFound().build();}
    @PostMapping("/api/test/users") public ResponseEntity<?> testCreate(@RequestBody RegisterRequest req){return testEnabled?create(req,"usuario"):ResponseEntity.notFound().build();}
    @DeleteMapping("/api/test/users/{id}") public ResponseEntity<?> delete(@PathVariable UUID id,@AuthenticationPrincipal AuthController.Principal p){
        if(!testEnabled)return ResponseEntity.notFound().build();
        if(p.id().equals(id))return ResponseEntity.status(409).body(AuthController.msg("No puedes eliminar la cuenta con la que iniciaste sesión."));
        var u=users.findById(id).orElse(null);if(u==null)return ResponseEntity.status(404).body(AuthController.msg("No se encontró el usuario."));
        if(u.role.equals("superusuario")&&users.countByRole("superusuario")<=1)return ResponseEntity.status(409).body(AuthController.msg("Debe permanecer al menos un superusuario."));
        users.delete(u);return ResponseEntity.noContent().build();
    }
    private ResponseEntity<?> create(RegisterRequest req,String role){
        String name=AuthController.norm(req.name()),email=AuthController.normEmail(req.email());
        if(name==null||name.length()>120||email==null||email.length()>254)return AuthController.bad("Nombre y correo válidos son obligatorios.");
        if(!AuthController.validPassword(req.password()))return AuthController.bad("La contraseña no cumple los requisitos de seguridad.");
        if(users.existsByEmail(email))return ResponseEntity.status(409).body(AuthController.msg("Ya existe una cuenta con ese correo."));
        var u=new UserEntity();u.name=name;u.email=email;u.role=role;u.passwordHash=passwords.encode(req.password());users.save(u);
        return ResponseEntity.status(201).body(AuthController.msg(role.equals("operador")?"Operador creado. Ya puede iniciar sesión.":"Cuenta creada."));
    }
}
