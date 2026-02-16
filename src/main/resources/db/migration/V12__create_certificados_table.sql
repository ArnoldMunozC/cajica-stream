-- Create certificados table
CREATE TABLE IF NOT EXISTS certificados (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    codigo_verificacion VARCHAR(50) NOT NULL UNIQUE,
    usuario_id BIGINT NOT NULL,
    curso_id BIGINT NOT NULL,
    fecha_emision DATETIME NOT NULL,
    nota_promedio DOUBLE,
    videos_completados INT,
    total_videos INT,
    quizzes_aprobados INT,
    total_quizzes INT,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id),
    FOREIGN KEY (curso_id) REFERENCES curso(id)
);

CREATE INDEX idx_certificados_codigo ON certificados(codigo_verificacion);
CREATE INDEX idx_certificados_usuario ON certificados(usuario_id);
CREATE INDEX idx_certificados_curso ON certificados(curso_id);
