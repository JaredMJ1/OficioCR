package com.example.oficiocr.message;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mensajes")
public class MensajeController {

    private final MensajeService mensajeService;

    public MensajeController(MensajeService mensajeService) {
        this.mensajeService = mensajeService;
    }

    @GetMapping("/conversacion/{conversacionId}")
    public List<Mensaje> obtenerPorConversacion(
            @PathVariable Long conversacionId
    ) {
        return mensajeService.obtenerPorConversacion(conversacionId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mensaje enviar(@RequestBody Mensaje mensaje) {
        return mensajeService.enviar(mensaje);
    }
}
