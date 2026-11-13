package com.example.oficiocr.notification;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notificaciones")
public class NotificacionController {

    private final NotificacionService notificacionService;

    public NotificacionController(NotificacionService notificacionService) {
        this.notificacionService = notificacionService;
    }

    @GetMapping("/usuario/{usuarioId}")
    public List<Notificacion> obtenerPorUsuario(
            @PathVariable Long usuarioId
    ) {
        return notificacionService.obtenerPorUsuario(usuarioId);
    }

    @GetMapping("/usuario/{usuarioId}/no-leidas")
    public List<Notificacion> obtenerNoLeidas(
            @PathVariable Long usuarioId
    ) {
        return notificacionService.obtenerNoLeidas(usuarioId);
    }

    @PatchMapping("/{id}/leer")
    public Notificacion marcarComoLeida(@PathVariable Long id) {
        return notificacionService.marcarComoLeida(id);
    }
}
