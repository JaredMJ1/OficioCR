package cr.oficiocr.api;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
@Entity @Table(name="\"Users\"")
public class UserEntity {
    @Id @Column(name="\"Id\"") public UUID id = UUID.randomUUID();
    @Column(name="\"Name\"", length=120, nullable=false) public String name;
    @Column(name="\"Email\"", length=254, nullable=false, unique=true) public String email;
    @Column(name="\"PasswordHash\"", nullable=false) public String passwordHash;
    @Column(name="\"Role\"", length=30, nullable=false) public String role;
    @Column(name="\"CreatedAt\"", nullable=false) public Instant createdAt = Instant.now();
    public UserEntity() {}
}
