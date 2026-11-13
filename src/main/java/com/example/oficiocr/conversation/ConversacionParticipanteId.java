package com.example.oficiocr.conversation;

import java.io.Serializable;
import java.util.Objects;

public class ConversacionParticipanteId implements Serializable {

    private Long conversacionId;
    private Long usuarioId;

    public ConversacionParticipanteId() {
    }

    public ConversacionParticipanteId(Long conversacionId, Long usuarioId) {
        this.conversacionId = conversacionId;
        this.usuarioId = usuarioId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ConversacionParticipanteId that)) return false;

        return Objects.equals(conversacionId, that.conversacionId)
                && Objects.equals(usuarioId, that.usuarioId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(conversacionId, usuarioId);
    }
}
