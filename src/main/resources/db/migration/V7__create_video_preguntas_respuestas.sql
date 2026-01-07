CREATE TABLE IF NOT EXISTS video_pregunta (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    curso_id BIGINT NOT NULL,
    video_id BIGINT NOT NULL,
    usuario_id BIGINT NOT NULL,
    titulo VARCHAR(255) NOT NULL,
    contenido TEXT NOT NULL,
    cerrada TINYINT(1) NOT NULL DEFAULT 0,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_video_pregunta_curso FOREIGN KEY (curso_id) REFERENCES curso(id),
    CONSTRAINT fk_video_pregunta_video FOREIGN KEY (video_id) REFERENCES video(id),
    CONSTRAINT fk_video_pregunta_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
);

CREATE INDEX idx_video_pregunta_video ON video_pregunta(video_id);

CREATE TABLE IF NOT EXISTS video_respuesta (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    pregunta_id BIGINT NOT NULL,
    usuario_id BIGINT NOT NULL,
    contenido TEXT NOT NULL,
    es_instructor TINYINT(1) NOT NULL DEFAULT 0,
    es_aceptada TINYINT(1) NOT NULL DEFAULT 0,
    visible TINYINT(1) NOT NULL DEFAULT 1,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_video_respuesta_pregunta FOREIGN KEY (pregunta_id) REFERENCES video_pregunta(id),
    CONSTRAINT fk_video_respuesta_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
);

CREATE INDEX idx_video_respuesta_pregunta ON video_respuesta(pregunta_id);
