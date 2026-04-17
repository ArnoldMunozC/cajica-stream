CREATE TABLE configuracion_sistema (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    clave VARCHAR(100) NOT NULL UNIQUE,
    valor TEXT,
    descripcion VARCHAR(300),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

INSERT INTO configuracion_sistema (clave, valor, descripcion) VALUES
('diploma.emisor.nombre', '', 'Nombre completo de quien emite el diploma'),
('diploma.emisor.cargo', '', 'Cargo de quien emite el diploma');
