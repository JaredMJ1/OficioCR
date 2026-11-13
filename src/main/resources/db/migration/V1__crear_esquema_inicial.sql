
-- =========================================================
-- OFICIOCR - ESQUEMA INICIAL
-- Flyway: V1__crear_esquema_inicial.sql
-- Motor: PostgreSQL
-- =========================================================

-- =========================================================
-- 1. ROLES
-- =========================================================

CREATE TABLE rol (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE,
    descripcion VARCHAR(255),
    activo BOOLEAN NOT NULL DEFAULT TRUE
);

-- =========================================================
-- 2. USUARIOS
-- =========================================================

CREATE TABLE usuario (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    rol_id BIGINT NOT NULL,
    nombre VARCHAR(150) NOT NULL,
    correo VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    telefono VARCHAR(30),
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    creado_en TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_usuario_rol
        FOREIGN KEY (rol_id) REFERENCES rol(id)
);

-- =========================================================
-- 3. CATEGORIAS
-- =========================================================

CREATE TABLE categoria (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE,
    descripcion TEXT,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    creado_en TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- =========================================================
-- 4. SERVICIOS
-- =========================================================

CREATE TABLE servicio (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    trabajador_id BIGINT NOT NULL,
    categoria_id BIGINT NOT NULL,
    nombre VARCHAR(150) NOT NULL,
    descripcion TEXT NOT NULL,
    precio NUMERIC(12,2) NOT NULL,
    zona VARCHAR(150) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    creado_en TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_servicio_trabajador
        FOREIGN KEY (trabajador_id) REFERENCES usuario(id),

    CONSTRAINT fk_servicio_categoria
        FOREIGN KEY (categoria_id) REFERENCES categoria(id),

    CONSTRAINT ck_servicio_precio
        CHECK (precio >= 0)
);

-- =========================================================
-- 5. ESTADOS DE SOLICITUD
-- =========================================================

CREATE TABLE estado_solicitud (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE,
    descripcion VARCHAR(255),
    activo BOOLEAN NOT NULL DEFAULT TRUE
);

-- =========================================================
-- 6. SOLICITUDES
-- =========================================================

CREATE TABLE solicitud (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    cliente_id BIGINT NOT NULL,
    servicio_id BIGINT NOT NULL,
    estado_solicitud_id BIGINT NOT NULL,
    descripcion TEXT NOT NULL,
    direccion VARCHAR(255) NOT NULL,
    fecha_solicitada DATE NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    creado_en TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_solicitud_cliente
        FOREIGN KEY (cliente_id) REFERENCES usuario(id),

    CONSTRAINT fk_solicitud_servicio
        FOREIGN KEY (servicio_id) REFERENCES servicio(id),

    CONSTRAINT fk_solicitud_estado
        FOREIGN KEY (estado_solicitud_id)
        REFERENCES estado_solicitud(id)
);

-- =========================================================
-- 7. CALIFICACIONES
-- =========================================================

CREATE TABLE calificacion (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    solicitud_id BIGINT NOT NULL UNIQUE,
    puntuacion SMALLINT NOT NULL,
    comentario TEXT,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    creado_en TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_calificacion_solicitud
        FOREIGN KEY (solicitud_id) REFERENCES solicitud(id),

    CONSTRAINT ck_calificacion_puntuacion
        CHECK (puntuacion BETWEEN 1 AND 5)
);

-- =========================================================
-- 8. ESTADOS DE PAGO
-- =========================================================

CREATE TABLE estado_pago (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE,
    descripcion VARCHAR(255),
    activo BOOLEAN NOT NULL DEFAULT TRUE
);

-- =========================================================
-- 9. PAGOS
-- =========================================================

CREATE TABLE pago (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    solicitud_id BIGINT NOT NULL,
    estado_pago_id BIGINT NOT NULL,
    monto NUMERIC(12,2) NOT NULL,
    referencia_externa VARCHAR(150) UNIQUE,
    es_prueba BOOLEAN NOT NULL DEFAULT TRUE,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    creado_en TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_pago_solicitud
        FOREIGN KEY (solicitud_id) REFERENCES solicitud(id),

    CONSTRAINT fk_pago_estado
        FOREIGN KEY (estado_pago_id) REFERENCES estado_pago(id),

    CONSTRAINT ck_pago_monto
        CHECK (monto > 0)
);

-- =========================================================
-- 10. CONVERSACIONES
-- =========================================================

CREATE TABLE conversacion (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    creada_en TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- =========================================================
-- 11. PARTICIPANTES DE CONVERSACION
-- =========================================================

CREATE TABLE conversacion_participante (
    conversacion_id BIGINT NOT NULL,
    usuario_id BIGINT NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    unido_en TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_conversacion_participante
        PRIMARY KEY (conversacion_id, usuario_id),

    CONSTRAINT fk_participante_conversacion
        FOREIGN KEY (conversacion_id)
        REFERENCES conversacion(id),

    CONSTRAINT fk_participante_usuario
        FOREIGN KEY (usuario_id)
        REFERENCES usuario(id)
);

-- =========================================================
-- 12. MENSAJES
-- =========================================================

CREATE TABLE mensaje (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    conversacion_id BIGINT NOT NULL,
    remitente_id BIGINT NOT NULL,
    contenido TEXT NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    enviado_en TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_mensaje_conversacion
        FOREIGN KEY (conversacion_id)
        REFERENCES conversacion(id),

    CONSTRAINT fk_mensaje_remitente
        FOREIGN KEY (conversacion_id, remitente_id)
        REFERENCES conversacion_participante(
            conversacion_id,
            usuario_id
        ),

    CONSTRAINT ck_mensaje_contenido
        CHECK (LENGTH(TRIM(contenido)) > 0)
);

-- =========================================================
-- 13. NOTIFICACIONES
-- =========================================================

CREATE TABLE notificacion (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    mensaje_id BIGINT,
    tipo VARCHAR(30) NOT NULL DEFAULT 'MENSAJE',
    contenido TEXT NOT NULL,
    leida BOOLEAN NOT NULL DEFAULT FALSE,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    creado_en TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_notificacion_usuario
        FOREIGN KEY (usuario_id)
        REFERENCES usuario(id),

    CONSTRAINT fk_notificacion_mensaje
        FOREIGN KEY (mensaje_id)
        REFERENCES mensaje(id),

    CONSTRAINT ck_notificacion_tipo
        CHECK (tipo IN (
            'MENSAJE',
            'SOLICITUD',
            'PAGO',
            'SISTEMA'
        ))
);

-- =========================================================
-- 14. RECOMENDACIONES DE IA
-- =========================================================

CREATE TABLE recomendacion_ia (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    consulta TEXT NOT NULL,
    respuesta TEXT,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    creado_en TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_recomendacion_usuario
        FOREIGN KEY (usuario_id)
        REFERENCES usuario(id)
);

-- =========================================================
-- 15. INDICES
-- =========================================================

CREATE INDEX idx_usuario_rol
    ON usuario(rol_id);

CREATE INDEX idx_servicio_trabajador
    ON servicio(trabajador_id);

CREATE INDEX idx_servicio_categoria
    ON servicio(categoria_id);

CREATE INDEX idx_solicitud_cliente
    ON solicitud(cliente_id);

CREATE INDEX idx_solicitud_servicio
    ON solicitud(servicio_id);

CREATE INDEX idx_solicitud_estado
    ON solicitud(estado_solicitud_id);

CREATE INDEX idx_pago_solicitud
    ON pago(solicitud_id);

CREATE INDEX idx_pago_estado
    ON pago(estado_pago_id);

CREATE INDEX idx_participante_usuario
    ON conversacion_participante(usuario_id);

CREATE INDEX idx_mensaje_conversacion
    ON mensaje(conversacion_id);

CREATE INDEX idx_mensaje_remitente
    ON mensaje(remitente_id);

CREATE INDEX idx_notificacion_usuario
    ON notificacion(usuario_id);

CREATE INDEX idx_notificacion_mensaje
    ON notificacion(mensaje_id);

CREATE INDEX idx_recomendacion_usuario
    ON recomendacion_ia(usuario_id);

-- =========================================================
-- 16. ACTUALIZACION AUTOMATICA DE FECHAS
-- =========================================================

CREATE OR REPLACE FUNCTION actualizar_fecha_modificacion()
RETURNS TRIGGER AS $$
BEGIN
    NEW.actualizado_en = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_usuario_actualizado
BEFORE UPDATE ON usuario
FOR EACH ROW
EXECUTE FUNCTION actualizar_fecha_modificacion();

CREATE TRIGGER trg_categoria_actualizado
BEFORE UPDATE ON categoria
FOR EACH ROW
EXECUTE FUNCTION actualizar_fecha_modificacion();

CREATE TRIGGER trg_servicio_actualizado
BEFORE UPDATE ON servicio
FOR EACH ROW
EXECUTE FUNCTION actualizar_fecha_modificacion();

CREATE TRIGGER trg_solicitud_actualizado
BEFORE UPDATE ON solicitud
FOR EACH ROW
EXECUTE FUNCTION actualizar_fecha_modificacion();

CREATE TRIGGER trg_pago_actualizado
BEFORE UPDATE ON pago
FOR EACH ROW
EXECUTE FUNCTION actualizar_fecha_modificacion();

-- =========================================================
-- 17. DATOS INICIALES: ROLES
-- =========================================================

INSERT INTO rol (nombre, descripcion)
VALUES
('CLIENTE', 'Usuario que solicita servicios'),
('TRABAJADOR', 'Profesional que ofrece servicios'),
('ADMINISTRADOR', 'Usuario encargado de administrar la plataforma');

-- =========================================================
-- 18. DATOS INICIALES: ESTADOS DE SOLICITUD
-- =========================================================

INSERT INTO estado_solicitud (nombre, descripcion)
VALUES
('PENDIENTE', 'Solicitud pendiente de respuesta'),
('ACEPTADA', 'Solicitud aceptada por el trabajador'),
('RECHAZADA', 'Solicitud rechazada'),
('EN_PROCESO', 'Servicio en ejecución'),
('COMPLETADA', 'Servicio finalizado'),
('CANCELADA', 'Solicitud cancelada');

-- =========================================================
-- 19. DATOS INICIALES: ESTADOS DE PAGO
-- =========================================================

INSERT INTO estado_pago (nombre, descripcion)
VALUES
('PENDIENTE', 'Pago pendiente'),
('PROCESANDO', 'Pago en procesamiento'),
('APROBADO', 'Pago realizado correctamente'),
('RECHAZADO', 'Pago rechazado'),
('REEMBOLSADO', 'Pago devuelto'),
('CANCELADO', 'Pago cancelado');
