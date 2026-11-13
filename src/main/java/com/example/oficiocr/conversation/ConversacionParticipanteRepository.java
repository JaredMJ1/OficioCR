package com.example.oficiocr.conversation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConversacionParticipanteRepository
        extends JpaRepository<ConversacionParticipante, ConversacionParticipanteId> {

    List<ConversacionParticipante> findByConversacionId(Long conversacionId);

    List<ConversacionParticipante> findByUsuarioIdAndActivoTrue(Long usuarioId);

    boolean existsByConversacionIdAndUsuarioIdAndActivoTrue(
            Long conversacionId,
            Long usuarioId
    );
}
