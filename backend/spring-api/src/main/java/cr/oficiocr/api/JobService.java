package cr.oficiocr.api;

import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class JobService {
    private final JobRepository jobs;

    public JobService(JobRepository jobs) { this.jobs = jobs; }

    public List<JobEntity> search(String search) {
        var rows = jobs.findAllByOrderByCreatedAtDesc();
        if (search == null || search.isBlank()) return rows.stream().limit(100).toList();
        String term = search.trim().toLowerCase(Locale.ROOT);
        return rows.stream()
                .filter(j -> (j.title + " " + j.description + " " + j.category + " " + j.location).toLowerCase(Locale.ROOT).contains(term))
                .limit(100).collect(Collectors.toList());
    }

    public Optional<JobEntity> findById(UUID id) { return jobs.findById(id); }

    public JobEntity save(JobEntity job) { return jobs.save(job); }
}
