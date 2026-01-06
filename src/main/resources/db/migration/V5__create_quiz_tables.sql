CREATE TABLE IF NOT EXISTS quiz (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    titulo VARCHAR(255) NOT NULL,
    seccion VARCHAR(100) NULL,
    max_intentos INT NOT NULL DEFAULT 1,
    activo TINYINT(1) NOT NULL DEFAULT 1,
    curso_id BIGINT NOT NULL,
    CONSTRAINT fk_quiz_curso FOREIGN KEY (curso_id) REFERENCES curso(id)
);

CREATE TABLE IF NOT EXISTS quiz_pregunta (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    quiz_id BIGINT NOT NULL,
    enunciado VARCHAR(1000) NOT NULL,
    tipo VARCHAR(20) NOT NULL,
    orden INT NULL,
    CONSTRAINT fk_quiz_pregunta_quiz FOREIGN KEY (quiz_id) REFERENCES quiz(id)
);

CREATE TABLE IF NOT EXISTS quiz_opcion (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    pregunta_id BIGINT NOT NULL,
    texto VARCHAR(1000) NOT NULL,
    correcta TINYINT(1) NOT NULL DEFAULT 0,
    orden INT NULL,
    CONSTRAINT fk_quiz_opcion_pregunta FOREIGN KEY (pregunta_id) REFERENCES quiz_pregunta(id)
);

CREATE TABLE IF NOT EXISTS quiz_intento (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    quiz_id BIGINT NOT NULL,
    usuario_id BIGINT NOT NULL,
    numero_intento INT NOT NULL,
    puntaje INT NOT NULL,
    total_preguntas INT NOT NULL,
    aprobado TINYINT(1) NOT NULL,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_quiz_intento_quiz FOREIGN KEY (quiz_id) REFERENCES quiz(id),
    CONSTRAINT fk_quiz_intento_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
);

CREATE TABLE IF NOT EXISTS quiz_respuesta (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    intento_id BIGINT NOT NULL,
    pregunta_id BIGINT NOT NULL,
    opcion_id BIGINT NULL,
    correcta TINYINT(1) NOT NULL,
    CONSTRAINT fk_quiz_respuesta_intento FOREIGN KEY (intento_id) REFERENCES quiz_intento(id),
    CONSTRAINT fk_quiz_respuesta_pregunta FOREIGN KEY (pregunta_id) REFERENCES quiz_pregunta(id),
    CONSTRAINT fk_quiz_respuesta_opcion FOREIGN KEY (opcion_id) REFERENCES quiz_opcion(id)
);
