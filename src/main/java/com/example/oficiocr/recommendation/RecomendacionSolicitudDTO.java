package com.example.oficiocr.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class RecomendacionSolicitudDTO {

    @NotNull(message = "Debe indicar el usuario")
    private Long usuarioId;

    @NotBlank(message = "La consulta no puede estar vacía")
    private String consulta;

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getConsulta() {
        return consulta;
    }

    public void setConsulta(String consulta) {
        this.consulta = consulta;
    }
}
