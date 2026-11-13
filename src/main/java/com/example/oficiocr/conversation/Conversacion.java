package com.example.oficiocr.conversation;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "conversacion")
public class Conversacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Boolean activo = true;

    @Column(name = "creada_en", nullable = false)
    private OffsetDateTime creadaEn;

    @PrePersist
    protected void onCreate() {
        creadaEn = OffsetDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public OffsetDateTime getCreadaEn() {
        return creadaEn;
    }
}
