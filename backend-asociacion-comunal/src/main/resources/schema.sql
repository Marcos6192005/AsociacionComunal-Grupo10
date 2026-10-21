CREATE TABLE IF NOT EXISTS usuario (
    id           VARCHAR(36)  PRIMARY KEY,
    nombre       VARCHAR(120) NOT NULL,
    correo       VARCHAR(160) NOT NULL UNIQUE,
    password     VARCHAR(100) NOT NULL,
    tipo         VARCHAR(20)  NOT NULL,
    numero_casa  VARCHAR(120),
    cargo        VARCHAR(40),
    CONSTRAINT usuario_tipo_chk CHECK (
        (tipo = 'VECINO' AND numero_casa IS NOT NULL AND cargo IS NULL)
        OR
        (tipo = 'DIRECTIVA' AND cargo IS NOT NULL AND numero_casa IS NULL)
    )
);

CREATE TABLE IF NOT EXISTS proyecto (
    id            VARCHAR(36)  PRIMARY KEY,
    nombre        VARCHAR(160) NOT NULL,
    descripcion   TEXT         NOT NULL,
    estado        VARCHAR(40)  NOT NULL,
    fecha_inicio  VARCHAR(20)  NOT NULL
);

CREATE TABLE IF NOT EXISTS voto (
    id              VARCHAR(36) PRIMARY KEY,
    proyecto_id     VARCHAR(36) NOT NULL REFERENCES proyecto (id),
    correo_usuario  VARCHAR(160) NOT NULL REFERENCES usuario (correo),
    valor           VARCHAR(20) NOT NULL,
    CONSTRAINT voto_unico UNIQUE (proyecto_id, correo_usuario)
);

CREATE TABLE IF NOT EXISTS comentario (
    id             VARCHAR(36)  PRIMARY KEY,
    proyecto_id    VARCHAR(36)  NOT NULL REFERENCES proyecto (id),
    correo_autor   VARCHAR(160) NOT NULL REFERENCES usuario (correo),
    nombre_autor   VARCHAR(120) NOT NULL,
    texto          TEXT         NOT NULL,
    fecha          VARCHAR(30)  NOT NULL
);

CREATE TABLE IF NOT EXISTS solicitud (
    id             VARCHAR(36)  PRIMARY KEY,
    titulo         VARCHAR(160) NOT NULL,
    descripcion    TEXT         NOT NULL,
    correo_autor   VARCHAR(160) NOT NULL REFERENCES usuario (correo),
    nombre_autor   VARCHAR(120) NOT NULL,
    estado         VARCHAR(30)  NOT NULL,
    respuesta      TEXT,
    proyecto_id    VARCHAR(36)  REFERENCES proyecto (id),
    fecha          VARCHAR(30)  NOT NULL
);

CREATE TABLE IF NOT EXISTS acta (
    id             VARCHAR(36)  PRIMARY KEY,
    titulo         VARCHAR(160) NOT NULL,
    contenido      TEXT         NOT NULL,
    fecha          VARCHAR(30)  NOT NULL,
    estado         VARCHAR(30)  NOT NULL,
    correo_autor   VARCHAR(160) NOT NULL REFERENCES usuario (correo),
    nombre_autor   VARCHAR(120) NOT NULL
);

CREATE TABLE IF NOT EXISTS acuerdo_acta (
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    acta_id      VARCHAR(36) NOT NULL REFERENCES acta (id) ON DELETE CASCADE,
    texto        TEXT        NOT NULL,
    proyecto_id  VARCHAR(36) REFERENCES proyecto (id),
    posicion     INT         NOT NULL
);

CREATE TABLE IF NOT EXISTS comunicado (
    id             VARCHAR(36)  PRIMARY KEY,
    titulo         VARCHAR(160) NOT NULL,
    contenido      TEXT         NOT NULL,
    fecha          VARCHAR(30)  NOT NULL,
    correo_autor   VARCHAR(160) NOT NULL REFERENCES usuario (correo),
    nombre_autor   VARCHAR(120) NOT NULL
);

CREATE TABLE IF NOT EXISTS cuota (
    id             VARCHAR(36)    PRIMARY KEY,
    correo_vecino  VARCHAR(160)   NOT NULL REFERENCES usuario (correo),
    nombre_vecino  VARCHAR(120)   NOT NULL,
    numero_casa    VARCHAR(120)   NOT NULL,
    periodo        VARCHAR(7)     NOT NULL,
    monto          NUMERIC(10, 2) NOT NULL,
    estado         VARCHAR(20)    NOT NULL,
    fecha_pago     VARCHAR(30),
    CONSTRAINT cuota_unica UNIQUE (correo_vecino, periodo)
);

CREATE TABLE IF NOT EXISTS movimiento (
    id               VARCHAR(36)    PRIMARY KEY,
    tipo             VARCHAR(20)    NOT NULL,
    concepto         VARCHAR(200)   NOT NULL,
    monto            NUMERIC(10, 2) NOT NULL,
    fecha            VARCHAR(30)    NOT NULL,
    proyecto_id      VARCHAR(36)    REFERENCES proyecto (id),
    correo_registro  VARCHAR(160)   NOT NULL REFERENCES usuario (correo),
    nombre_registro  VARCHAR(120)   NOT NULL,
    cuota_id         VARCHAR(36)    REFERENCES cuota (id)
);

CREATE TABLE IF NOT EXISTS registro_email (
    id                VARCHAR(36)  PRIMARY KEY,
    destinatario      VARCHAR(160) NOT NULL,
    asunto            VARCHAR(200) NOT NULL,
    fecha_creacion    TIMESTAMP    NOT NULL,
    estado            VARCHAR(20)  NOT NULL,
    intentos          INT          NOT NULL DEFAULT 0,
    fecha_envio       TIMESTAMP,
    ultimo_error      TEXT,
    hilo_procesador   VARCHAR(80),
    CONSTRAINT registro_email_estado_chk CHECK (estado IN ('PENDIENTE', 'ENVIADO', 'FALLIDO'))
);