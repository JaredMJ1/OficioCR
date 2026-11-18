package cr.oficiocr.api;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface JobRepository extends JpaRepository<JobEntity, UUID> {
    List<JobEntity> findAllByOrderByCreatedAtDesc();
}
