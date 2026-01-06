CREATE TABLE IF NOT EXISTS material_pdf (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    titulo VARCHAR(255) NOT NULL,
    seccion VARCHAR(100) NULL,
    pdf_file_path VARCHAR(255) NOT NULL,
    orden INT NULL,
    curso_id BIGINT NOT NULL,
    CONSTRAINT fk_material_pdf_curso FOREIGN KEY (curso_id) REFERENCES curso(id)
);
