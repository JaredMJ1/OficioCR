package com.example.oficiocr.conversation;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/conversaciones")
public class ConversacionController {

    private final ConversacionService conversacionService;

    public ConversacionController(ConversacionService conversacionService) {
        this.conversacionService = conversacionService;
    }

    @GetMapping
    public List<Conversacion> obtenerActivas() {
        return conversacionService.obtenerActivas();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Conversacion crear(
            @RequestParam Long usuario1Id,
            @RequestParam Long usuario2Id
    ) {
        return conversacionService.crearConversacion(
                usuario1Id,
                usuario2Id
        );
    }
}
