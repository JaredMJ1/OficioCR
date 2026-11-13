package com.example.oficiocr.notification;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificacionService {

    private final NotificacionRepository notificacionRepository;

    public NotificacionService(NotificacionRepository notificacionRepository) {
        this.notificacionRepository = notificacionRepository;
    }

    public List<Notificacion> obtenerPorUsuario(Long usuarioId) {
        return notificacionRepository
                .findByUsuarioIdAndActivoTrueOrderByCreadoEnDesc(usuarioId);
    }

    public List<Notificacion> obtenerNoLeidas(Long usuarioId) {
        return notificacionRepository
                .findByUsuarioIdAndLeidaFalseAndActivoTrue(usuarioId);
    }

    @Transactional
    public Notificacion marcarComoLeida(Long id) {
        Notificacion notificacion = notificacionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No se encontró la notificación"
                ));

        notificacion.setLeida(true);

        return notificacionRepository.save(notificacion);
    }
}
