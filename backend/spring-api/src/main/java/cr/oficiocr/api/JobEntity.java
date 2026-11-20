package cr.oficiocr.api;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
@Entity @Table(name="\"JobListings\"")
public class JobEntity {
    @Id @Column(name="\"Id\"") public UUID id = UUID.randomUUID();
    @Column(name="\"Title\"", length=120, nullable=false) public String title;
    @Column(name="\"Description\"", length=2000, nullable=false) public String description;
    @Column(name="\"Category\"", length=60, nullable=false) public String category;
    @Column(name="\"Location\"", length=100, nullable=false) public String location;
    @Column(name="\"AuthorId\"", nullable=false) public UUID authorId;
    @Column(name="\"AuthorName\"", length=120, nullable=false) public String authorName;
    @Column(name="\"AcceptedById\"") public UUID acceptedById;
    @Column(name="\"AcceptedByName\"", length=120) public String acceptedByName;
    @Column(name="\"CreatedAt\"", nullable=false) public Instant createdAt = Instant.now();
    public JobEntity() {}
}
