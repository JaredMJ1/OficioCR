package com.example.oficiocr.conversation;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ConversacionService {

    private final ConversacionRepository conversacionRepository;
    private final ConversacionParticipanteRepository participanteRepository;

    public ConversacionService(
            ConversacionRepository conversacionRepository,
            ConversacionParticipanteRepository participanteRepository
    ) {
        this.conversacionRepository = conversacionRepository;
        this.participanteRepository = participanteRepository;
    }

    @Transactional
    public Conversacion crearConversacion(Long usuario1Id, Long usuario2Id) {

        if (usuario1Id == null || usuario2Id == null) {
            throw new IllegalArgumentException("Debe indicar ambos usuarios");
        }

        if (usuario1Id.equals(usuario2Id)) {
            throw new IllegalArgumentException(
                    "Los participantes deben ser usuarios diferentes"
            );
        }

        Conversacion conversacion = conversacionRepository.save(
                new Conversacion()
        );

        ConversacionParticipante participante1 =
                new ConversacionParticipante();

        participante1.setConversacionId(conversacion.getId());
        participante1.setUsuarioId(usuario1Id);
        participante1.setActivo(true);

        ConversacionParticipante participante2 =
                new ConversacionParticipante();

        participante2.setConversacionId(conversacion.getId());
        participante2.setUsuarioId(usuario2Id);
        participante2.setActivo(true);

        participanteRepository.saveAll(
                List.of(participante1, participante2)
        );

        return conversacion;
    }

    public List<Conversacion> obtenerActivas() {
        return conversacionRepository.findByActivoTrue();
    }
}
