package cr.oficiocr.api;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserRepository users; private final PasswordEncoder passwords; private final JavaMailSender mail;
    private final String frontend, smtpFrom, smtpHost, smtpUsername, environment, sessionCookieName;
    private final boolean secureCookies;
    private final Map<String, ResetToken> resetTokens = new ConcurrentHashMap<>();
    private static final String CTX_KEY = HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY;
    record Principal(UUID id, String name, String email, String role) implements java.io.Serializable {}
    private record ResetToken(String email, Instant expires) {}

    public AuthController(UserRepository users, PasswordEncoder passwords, JavaMailSender mail,
            @Value("${app.frontend-origin}") String frontend, @Value("${app.smtp-from}") String smtpFrom,
            @Value("${spring.mail.host:}") String smtpHost, @Value("${spring.mail.username:}") String smtpUsername,
            @Value("${app.environment}") String environment, @Value("${server.servlet.session.cookie.name}") String sessionCookieName,
            @Value("${SESSION_COOKIE_SECURE:false}") boolean secureCookies) {
        this.users=users; this.passwords=passwords; this.mail=mail; this.frontend=frontend; this.smtpFrom=smtpFrom; this.smtpHost=smtpHost; this.smtpUsername=smtpUsername; this.environment=environment; this.sessionCookieName=sessionCookieName; this.secureCookies=secureCookies;
    }
    @GetMapping("/csrf") public Map<String,String> csrf(CsrfToken token) { return Map.of("requestToken", token.getToken()); }

    @PostMapping("/register") public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
        String name=norm(req.name()), email=normEmail(req.email());
        if(name==null || name.length()>120 || email==null || email.length()>254) return bad("Nombre y correo válidos son obligatorios.");
        if(!validPassword(req.password())) return bad("La contraseña debe cumplir todos los requisitos indicados.");
        if(users.existsByEmail(email)) return ResponseEntity.status(409).body(msg("Ya existe una cuenta con ese correo."));
        var u=new UserEntity(); u.name=name; u.email=email; u.role="usuario"; u.passwordHash=passwords.encode(req.password()); users.save(u);
        return ResponseEntity.status(201).body(msg("Cuenta creada. Ya puedes iniciar sesión."));
    }
    @PostMapping("/login") public ResponseEntity<?> login(@RequestBody LoginRequest req, HttpServletRequest request) {
        var u=users.findByEmail(normEmail(req.email())).orElse(null);
        if(u==null || req.password()==null || !passwords.matches(req.password(),u.passwordHash)) return ResponseEntity.status(401).body(msg("Correo o contraseña incorrectos."));
        if(request.getSession(false)!=null) request.changeSessionId(); else request.getSession(true);
        var auth=new UsernamePasswordAuthenticationToken(new Principal(u.id,u.name,u.email,u.role),null,List.of(new SimpleGrantedAuthority("ROLE_"+u.role)));
        SecurityContext ctx=SecurityContextHolder.createEmptyContext(); ctx.setAuthentication(auth); SecurityContextHolder.setContext(ctx);
        request.getSession(true).setAttribute(CTX_KEY,ctx);
        return ResponseEntity.ok(new SessionResponse(Instant.now().plusSeconds(1800),new UserResponse(u)));
    }
    @PostMapping("/logout") public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        var session=request.getSession(false); if(session!=null) session.invalidate(); SecurityContextHolder.clearContext();
        var cookie=new jakarta.servlet.http.Cookie(sessionCookieName,""); cookie.setPath("/"); cookie.setMaxAge(0); cookie.setHttpOnly(true); cookie.setSecure(secureCookies); response.addCookie(cookie);
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/me") public ResponseEntity<?> me(@org.springframework.security.core.annotation.AuthenticationPrincipal Principal p) {
        if(p==null)return ResponseEntity.status(401).body(msg("No autorizado."));
        return users.findById(p.id()).<ResponseEntity<?>>map(u->ResponseEntity.ok(new UserResponse(u))).orElseGet(()->ResponseEntity.status(401).body(msg("No autorizado.")));
    }
    @PutMapping("/me") public ResponseEntity<?> update(@RequestBody UpdateProfileRequest req, @org.springframework.security.core.annotation.AuthenticationPrincipal Principal p, HttpServletRequest request) {
        String name=norm(req.name()),email=normEmail(req.email());
        if(name==null||name.length()>120||email==null||email.length()>254)return bad("Nombre y correo válidos son obligatorios.");
        if(users.existsByEmailAndIdNot(email,p.id()))return ResponseEntity.status(409).body(msg("Ya existe una cuenta con ese correo."));
        var u=users.findById(p.id()).orElse(null); if(u==null)return ResponseEntity.status(401).body(msg("No autorizado."));
        u.name=name;u.email=email;users.save(u);
        var auth=new UsernamePasswordAuthenticationToken(new Principal(u.id,u.name,u.email,u.role),null,List.of(new SimpleGrantedAuthority("ROLE_"+u.role)));
        var ctx=SecurityContextHolder.createEmptyContext();ctx.setAuthentication(auth);SecurityContextHolder.setContext(ctx);request.getSession().setAttribute(CTX_KEY,ctx);
        return ResponseEntity.ok(new UserResponse(u));
    }
    @PostMapping("/password/forgot") public ResponseEntity<?> forgot(@RequestBody EmailRequest req) {
        var email=normEmail(req.email()); var u=email==null?null:users.findByEmail(email).orElse(null);
        if(u!=null) try { sendReset(u); } catch(Exception e) { return ResponseEntity.status(503).body(msg(environment.equalsIgnoreCase("development")?"No se pudo enviar el correo de recuperación.":"No se pudo completar el envío SMTP.")); }
        return ResponseEntity.ok(msg("Si existe una cuenta con ese correo, recibirás instrucciones."));
    }
    @PostMapping("/password/reset") public ResponseEntity<?> reset(@RequestBody ResetPasswordRequest req) {
        String email=normEmail(req.email()); if(email==null||!validPassword(req.password()))return bad("Correo o contraseña no válidos. La contraseña debe cumplir todos los requisitos.");
        String key=tokenKey(req.token());var entry=resetTokens.remove(key);var u=users.findByEmail(email).orElse(null);
        if(u==null||entry==null||!entry.email.equals(email)||entry.expires.isBefore(Instant.now()))return bad("El código no es válido o venció. Solicita otro.");
        u.passwordHash=passwords.encode(req.password());users.save(u);return ResponseEntity.ok(msg("Contraseña actualizada. Ya puedes iniciar sesión."));
    }
    private void sendReset(UserEntity u) throws Exception {
        byte[] bytes=new byte[32];new SecureRandom().nextBytes(bytes);String token=HexFormat.of().formatHex(bytes).toUpperCase(Locale.ROOT);
        resetTokens.put(tokenKey(token),new ResetToken(u.email,Instant.now().plusSeconds(86400)));
        String origin=frontend.split(",")[0].trim().replaceAll("/$", "");
        String link=origin+"/recuperar-contrasena?email="+URLEncoder.encode(u.email,StandardCharsets.UTF_8)+"&token="+token;
        if(smtpHost.isBlank() && environment.equalsIgnoreCase("development")) { System.out.println("[correo de desarrollo] Para: "+u.email+" | token: "+token+" | "+link);return; }
        if(smtpHost.isBlank()) throw new IllegalStateException("Configura SMTP_HOST, SMTP_USERNAME y SMTP_PASSWORD para enviar correo.");
        var message=new SimpleMailMessage();message.setFrom(!smtpFrom.isBlank()?smtpFrom:(!smtpUsername.isBlank()?smtpUsername:"no-reply@oficiocr.local"));message.setTo(u.email);message.setSubject("Recupera tu acceso a OficioCR");message.setText("Hola "+u.name+",\n\nTu código temporal es: "+token+"\nTambién puedes abrir: "+link+"\nVence en 24 horas.");mail.send(message);
    }
    private static String tokenKey(String token) { if(token==null)return ""; try{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8)));}catch(Exception e){throw new IllegalStateException(e);} }
    static boolean validPassword(String p) { return p!=null&&p.length()>=10&&p.length()<=128&&p.chars().anyMatch(c->c>='A'&&c<='Z')&&p.chars().anyMatch(c->c>='a'&&c<='z')&&p.chars().anyMatch(c->c>='0'&&c<='9')&&p.chars().anyMatch(c->(c>='!'&&c<='/')||(c>=':'&&c<='@')||(c>='['&&c<='`')||(c>='{'&&c<='~')); }
    static String norm(String s){if(s==null)return null;s=s.trim();return s.isEmpty()?null:s;}
    static String normEmail(String s){String n=norm(s);return n==null?null:n.toLowerCase(Locale.ROOT);}
    static ResponseEntity<?> bad(String s){return ResponseEntity.badRequest().body(msg(s));}
    static MessageResponse msg(String s){return new MessageResponse(s);}
}
