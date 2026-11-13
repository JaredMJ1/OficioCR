package com.example.oficiocr.message;

import com.example.oficiocr.conversation.Conversacion;
import com.example.oficiocr.conversation.ConversacionRepository;
import com.example.oficiocr.conversation.ConversacionParticipante;
import com.example.oficiocr.conversation.ConversacionParticipanteRepository;
import com.example.oficiocr.notification.Notificacion;
import com.example.oficiocr.notification.NotificacionRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class MensajeService {

    private final MensajeRepository mensajeRepository;
    private final ConversacionRepository conversacionRepository;
    private final ConversacionParticipanteRepository participanteRepository;
    private final NotificacionRepository notificacionRepository;

    public MensajeService(
            MensajeRepository mensajeRepository,
            ConversacionRepository conversacionRepository,
            ConversacionParticipanteRepository participanteRepository,
            NotificacionRepository notificacionRepository
    ) {
        this.mensajeRepository = mensajeRepository;
        this.conversacionRepository = conversacionRepository;
        this.participanteRepository = participanteRepository;
        this.notificacionRepository = notificacionRepository;
    }

    public List<Mensaje> obtenerPorConversacion(Long conversacionId) {
        return mensajeRepository
                .findByConversacionIdAndActivoTrueOrderByEnviadoEnAsc(
                        conversacionId
                );
    }

    @Transactional
    public Mensaje enviar(Mensaje mensaje) {

        if (mensaje.getContenido() == null ||
                mensaje.getContenido().isBlank()) {
            throw new IllegalArgumentException(
                    "El mensaje no puede estar vacío"
            );
        }

        if (mensaje.getConversacionId() == null ||
                mensaje.getRemitenteId() == null) {
            throw new IllegalArgumentException(
                    "Debe indicar la conversación y el remitente"
            );
        }

        Conversacion conversacion = conversacionRepository
                .findById(mensaje.getConversacionId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "La conversación no existe"
                ));

        if (!Boolean.TRUE.equals(conversacion.getActivo())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "No se pueden enviar mensajes a una conversación inactiva"
            );
        }

        boolean esParticipante =
                participanteRepository.existsByConversacionIdAndUsuarioIdAndActivoTrue(
                        mensaje.getConversacionId(),
                        mensaje.getRemitenteId()
                );

        if (!esParticipante) {
            throw new IllegalArgumentException(
                    "El remitente no pertenece a esta conversación"
            );
        }

        mensaje.setActivo(true);

        Mensaje mensajeGuardado = mensajeRepository.save(mensaje);

        List<ConversacionParticipante> participantes =
                participanteRepository.findByConversacionId(
                        mensaje.getConversacionId()
                );

        for (ConversacionParticipante participante : participantes) {

            if (Boolean.TRUE.equals(participante.getActivo())
                    && !participante.getUsuarioId().equals(
                    mensaje.getRemitenteId()
            )) {

                Notificacion notificacion = new Notificacion();

                notificacion.setUsuarioId(participante.getUsuarioId());
                notificacion.setMensajeId(mensajeGuardado.getId());
                notificacion.setTipo("MENSAJE");

                notificacion.setContenido(
                        "Tienes un nuevo mensaje en la conversación #"
                                + mensaje.getConversacionId()
                );

                notificacion.setLeida(false);
                notificacion.setActivo(true);

                notificacionRepository.save(notificacion);
            }
        }

        return mensajeGuardado;
    }
}
