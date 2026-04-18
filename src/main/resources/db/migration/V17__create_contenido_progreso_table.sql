CREATE TABLE contenido_progreso (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    curso_id BIGINT NOT NULL,
    tipo VARCHAR(10) NOT NULL,
    contenido_id BIGINT NOT NULL,
    fecha_completado TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_contenido_progreso UNIQUE (usuario_id, tipo, contenido_id)
);
