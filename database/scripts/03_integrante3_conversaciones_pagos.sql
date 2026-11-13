-- OficioCR · Integrante 3 · Esquema de referencia (SQL Server)
-- Conversaciones, mensajes, notificaciones, pagos de PRUEBA y recomendaciones IA (opcional).
-- OJO: el esquema real se versiona con migraciones de EF Core; este script es borrador
-- para acordar nombres/tipos con el equipo. Requiere que existan USUARIO y SOLICITUD.

IF OBJECT_ID('dbo.CONVERSACION') IS NULL
CREATE TABLE dbo.CONVERSACION (
    Id        INT IDENTITY(1,1) PRIMARY KEY,
    CreadaEn  DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME()
);

IF OBJECT_ID('dbo.CONVERSACION_PARTICIPANTE') IS NULL
CREATE TABLE dbo.CONVERSACION_PARTICIPANTE (
    ConversacionId INT NOT NULL REFERENCES dbo.CONVERSACION(Id) ON DELETE CASCADE,
    UsuarioId      INT NOT NULL REFERENCES dbo.USUARIO(Id),
    PRIMARY KEY (ConversacionId, UsuarioId)
);

IF OBJECT_ID('dbo.MENSAJE') IS NULL
CREATE TABLE dbo.MENSAJE (
    Id             INT IDENTITY(1,1) PRIMARY KEY,
    ConversacionId INT NOT NULL REFERENCES dbo.CONVERSACION(Id) ON DELETE CASCADE,
    RemitenteId    INT NOT NULL REFERENCES dbo.USUARIO(Id),
    Contenido      NVARCHAR(1000) NOT NULL,
    EnviadoEn      DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
    Leido          BIT NOT NULL DEFAULT 0
);
CREATE INDEX IX_MENSAJE_Conversacion_EnviadoEn ON dbo.MENSAJE (ConversacionId, EnviadoEn);

IF OBJECT_ID('dbo.NOTIFICACION') IS NULL
CREATE TABLE dbo.NOTIFICACION (
    Id        INT IDENTITY(1,1) PRIMARY KEY,
    UsuarioId INT NOT NULL REFERENCES dbo.USUARIO(Id),
    Tipo      NVARCHAR(20)  NOT NULL,
    Contenido NVARCHAR(300) NOT NULL,
    Leida     BIT NOT NULL DEFAULT 0,
    CreadaEn  DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME()
);
CREATE INDEX IX_NOTIFICACION_Usuario_Leida ON dbo.NOTIFICACION (UsuarioId, Leida);

IF OBJECT_ID('dbo.PAGO') IS NULL
CREATE TABLE dbo.PAGO (
    Id                INT IDENTITY(1,1) PRIMARY KEY,
    SolicitudId       INT NOT NULL REFERENCES dbo.SOLICITUD(Id),
    Monto             DECIMAL(12,2) NOT NULL CHECK (Monto > 0),
    Estado            NVARCHAR(20) NOT NULL
                      CHECK (Estado IN ('PENDIENTE','APROBADO','RECHAZADO','CANCELADO')),
    ReferenciaExterna NVARCHAR(100) NULL,
    ClaveIdempotencia NVARCHAR(64)  NOT NULL,
    EsPrueba          BIT NOT NULL DEFAULT 1,
    CreadoEn          DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
    CONSTRAINT UQ_PAGO_ClaveIdempotencia UNIQUE (ClaveIdempotencia)
);
-- No se guarda CVV ni número de tarjeta (RNF-08).

-- Opcional: solo si se decide guardar las consultas a la IA
IF OBJECT_ID('dbo.RECOMENDACION_IA') IS NULL
CREATE TABLE dbo.RECOMENDACION_IA (
    Id                 INT IDENTITY(1,1) PRIMARY KEY,
    UsuarioId          INT NOT NULL REFERENCES dbo.USUARIO(Id),
    Consulta           NVARCHAR(500) NOT NULL,
    CategoriaDetectada NVARCHAR(100) NULL,
    CreadaEn           DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME()
);
