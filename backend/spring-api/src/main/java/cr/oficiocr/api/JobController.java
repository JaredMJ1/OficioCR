package cr.oficiocr.api;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/jobs")
public class JobController {
    private final JobService jobs;
    public JobController(JobService jobs){this.jobs=jobs;}
    @GetMapping public List<JobEntity> search(@RequestParam(required=false) String search){
        return jobs.search(search);
    }
    @PostMapping public ResponseEntity<?> create(@RequestBody CreateJobRequest req,@AuthenticationPrincipal AuthController.Principal p){
        String title=AuthController.norm(req.title()),description=AuthController.norm(req.description()),category=AuthController.norm(req.category()),location=AuthController.norm(req.location());
        if(title==null||title.length()>120||description==null||description.length()>2000||category==null||category.length()>60||location==null||location.length()>100)return AuthController.bad("Completa todos los campos respetando sus longitudes máximas.");
        var j=new JobEntity();j.title=title;j.description=description;j.category=category;j.location=location;j.authorId=p.id();j.authorName=p.name()==null?"Usuario OficioCR":p.name();jobs.save(j);
        return ResponseEntity.created(java.net.URI.create("/api/jobs/"+j.id)).body(j);
    }
    @PostMapping("/{id}/accept") public ResponseEntity<?> accept(@PathVariable UUID id,@AuthenticationPrincipal AuthController.Principal p){
        var j=jobs.findById(id).orElse(null);if(j==null)return ResponseEntity.status(404).body(AuthController.msg("No se encontró el anuncio."));
        if(j.acceptedById!=null)return ResponseEntity.status(409).body(AuthController.msg("Este trabajo ya fue aceptado."));
        if(j.authorId.equals(p.id()))return AuthController.bad("No puedes aceptar tu propio anuncio.");
        j.acceptedById=p.id();j.acceptedByName=p.name()==null?"Usuario OficioCR":p.name();jobs.save(j);return ResponseEntity.ok(j);
    }
}
