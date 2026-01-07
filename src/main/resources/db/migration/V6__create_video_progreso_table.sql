CREATE TABLE IF NOT EXISTS video_progreso (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    curso_id BIGINT NOT NULL,
    video_id BIGINT NOT NULL,
    segundo DOUBLE NOT NULL DEFAULT 0,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_video_progreso_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id),
    CONSTRAINT fk_video_progreso_curso FOREIGN KEY (curso_id) REFERENCES curso(id),
    CONSTRAINT fk_video_progreso_video FOREIGN KEY (video_id) REFERENCES video(id),
    CONSTRAINT uk_video_progreso_usuario_curso UNIQUE (usuario_id, curso_id)
);
