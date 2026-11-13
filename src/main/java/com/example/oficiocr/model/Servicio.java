package com.example.oficiocr.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Getter
@Setter
@Table(name = "servicio")
public class Servicio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    int trabajador_id;
    @Column(nullable = false)
    int categoria_id;
    @Column(nullable = false, length = 100)
    String nombre;
    @Column(nullable = false,length = 255)
    String descripcion;
    @Column(nullable = false, precision = 12, scale = 2)
    BigDecimal precio;
    @Column(nullable = false,length = 150)
    String zona;
    @Column(nullable = false)
    Boolean activo;
    @Column(name = "creado_en", nullable = false,updatable = false)
    OffsetDateTime creadoEn;
    @Column(name = "actualizado_en", nullable = false,updatable = false)
    OffsetDateTime actualizadoEn;
}
