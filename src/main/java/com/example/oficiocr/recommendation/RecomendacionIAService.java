package com.example.oficiocr.recommendation;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RecomendacionIAService {

    private final RecomendacionIARepository recomendacionRepository;

    public RecomendacionIAService(
            RecomendacionIARepository recomendacionRepository
    ) {
        this.recomendacionRepository = recomendacionRepository;
    }

    public List<RecomendacionIA> obtenerPorUsuario(Long usuarioId) {
        return recomendacionRepository
                .findByUsuarioIdAndActivoTrueOrderByCreadoEnDesc(usuarioId);
    }

    public List<RecomendacionIA> obtenerTodas() {
        return recomendacionRepository
                .findByActivoTrueOrderByCreadoEnDesc();
    }

    @Transactional
    public RecomendacionIA guardarConsulta(
            Long usuarioId,
            String consulta
    ) {
        if (usuarioId == null) {
            throw new IllegalArgumentException(
                    "Debe indicar el usuario"
            );
        }

        if (consulta == null || consulta.isBlank()) {
            throw new IllegalArgumentException(
                    "La consulta no puede estar vacía"
            );
        }

        RecomendacionIA recomendacion = new RecomendacionIA();

        recomendacion.setUsuarioId(usuarioId);
        recomendacion.setConsulta(consulta.trim());
        recomendacion.setActivo(true);

        return recomendacionRepository.save(recomendacion);
    }

    @Transactional
    public RecomendacionIA guardarRespuesta(
            Long recomendacionId,
            String respuesta
    ) {
        if (respuesta == null || respuesta.isBlank()) {
            throw new IllegalArgumentException(
                    "La respuesta no puede estar vacía"
            );
        }

        RecomendacionIA recomendacion = recomendacionRepository
                .findById(recomendacionId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "La recomendación no existe"
                ));

        recomendacion.setRespuesta(respuesta.trim());

        return recomendacionRepository.save(recomendacion);
    }

    @Transactional
    public void desactivar(Long recomendacionId) {
        RecomendacionIA recomendacion = recomendacionRepository
                .findById(recomendacionId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "La recomendación no existe"
                ));

        recomendacion.setActivo(false);
        recomendacionRepository.save(recomendacion);
    }
}
