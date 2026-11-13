package com.example.oficiocr.conversation;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.OffsetDateTime;

@Entity
@Table(name = "conversacion_participante")
@IdClass(ConversacionParticipanteId.class)
public class ConversacionParticipante {

    @Id
    @Column(name = "conversacion_id")
    private Long conversacionId;

    @Id
    @Column(name = "usuario_id")
    private Long usuarioId;

    @Column(nullable = false)
    private Boolean activo = true;

    @Column(name = "unido_en", nullable = false)
    private OffsetDateTime unidoEn;

    @PrePersist
    protected void onCreate() {
        unidoEn = OffsetDateTime.now();
    }

    public Long getConversacionId() {
        return conversacionId;
    }

    public void setConversacionId(Long conversacionId) {
        this.conversacionId = conversacionId;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public OffsetDateTime getUnidoEn() {
        return unidoEn;
    }
}
