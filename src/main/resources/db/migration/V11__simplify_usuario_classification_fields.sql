-- Simplify classification fields: drop multiple booleans, add single dropdown field
ALTER TABLE usuarios DROP COLUMN mujer_cabeza_familia;
ALTER TABLE usuarios DROP COLUMN discapacidad;
ALTER TABLE usuarios DROP COLUMN victima_conflicto_armado;
ALTER TABLE usuarios DROP COLUMN poblacion_migrante;
ALTER TABLE usuarios DROP COLUMN pertenencia_etnica;

-- Add single classification field
ALTER TABLE usuarios ADD COLUMN clasificacion_poblacional VARCHAR(50);

-- Expand enfoque_genero to accommodate longer values
ALTER TABLE usuarios MODIFY COLUMN enfoque_genero VARCHAR(20);
