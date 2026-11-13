package com.example.oficiocr.recommendation;

import com.example.oficiocr.dto.RecomendacionSolicitudDTO;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recomendaciones")
public class RecomendacionIAController {

    private final RecomendacionIAService recomendacionService;

    public RecomendacionIAController(
            RecomendacionIAService recomendacionService
    ) {
        this.recomendacionService = recomendacionService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RecomendacionIA crearConsulta(
            @Valid @RequestBody RecomendacionSolicitudDTO solicitud
    ) {
        return recomendacionService.guardarConsulta(
                solicitud.getUsuarioId(),
                solicitud.getConsulta()
        );
    }

    @GetMapping("/usuario/{usuarioId}")
    public List<RecomendacionIA> obtenerPorUsuario(
            @PathVariable Long usuarioId
    ) {
        return recomendacionService.obtenerPorUsuario(usuarioId);
    }

    @GetMapping
    public List<RecomendacionIA> obtenerTodas() {
        return recomendacionService.obtenerTodas();
    }

    @PatchMapping("/{id}/respuesta")
    public RecomendacionIA guardarRespuesta(
            @PathVariable Long id,
            @RequestBody String respuesta
    ) {
        return recomendacionService.guardarRespuesta(id, respuesta);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void desactivar(@PathVariable Long id) {
        recomendacionService.desactivar(id);
    }
}
