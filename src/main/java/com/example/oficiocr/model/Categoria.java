package com.example.oficiocr.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Getter
@Setter
@Table(name = "categoria")
public class Categoria {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false,length = 100)
    String nombre;
    @Column(nullable = false,length = 255)
    String descripcion;
    @Column(nullable = false)
    Boolean activo;
    @Column(name = "creado_en", nullable = false,updatable = false)
    OffsetDateTime creadoEn;
    @Column(name = "actualizado_en", nullable = false,updatable = false)
    OffsetDateTime actualizadoEn;

    @PrePersist
    protected void onCreate() {
        OffsetDateTime ahora = OffsetDateTime.now();
        creadoEn = ahora;
        actualizadoEn = ahora;
    }

    @PreUpdate
    protected void onUpdate() {
        actualizadoEn = OffsetDateTime.now();
    }
}
