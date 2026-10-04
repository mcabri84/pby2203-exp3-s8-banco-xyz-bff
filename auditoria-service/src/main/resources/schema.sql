CREATE TABLE IF NOT EXISTS eventos_procesados (
    evento_id VARCHAR(36) PRIMARY KEY,
    transaccion_id VARCHAR(64) NOT NULL,
    tipo_evento VARCHAR(64) NOT NULL,
    contenido CLOB NOT NULL,
    procesado_en VARCHAR(64) NOT NULL
);
