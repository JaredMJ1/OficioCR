package com.example.oficiocr.message;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "mensaje")
public class Mensaje {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "conversacion_id", nullable = false)
    private Long conversacionId;

    @Column(name = "remitente_id", nullable = false)
    private Long remitenteId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String contenido;

    @Column(nullable = false)
    private Boolean activo = true;

    @Column(name = "enviado_en", nullable = false)
    private OffsetDateTime enviadoEn;

    @PrePersist
    protected void onCreate() {
        enviadoEn = OffsetDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Long getConversacionId() {
        return conversacionId;
    }

    public void setConversacionId(Long conversacionId) {
        this.conversacionId = conversacionId;
    }

    public Long getRemitenteId() {
        return remitenteId;
    }

    public void setRemitenteId(Long remitenteId) {
        this.remitenteId = remitenteId;
    }

    public String getContenido() {
        return contenido;
    }

    public void setContenido(String contenido) {
        this.contenido = contenido;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public OffsetDateTime getEnviadoEn() {
        return enviadoEn;
    }
}
