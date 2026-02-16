-- Add registration fields to usuarios table
ALTER TABLE usuarios ADD COLUMN numero_identificacion VARCHAR(20);
ALTER TABLE usuarios ADD COLUMN edad INT;
ALTER TABLE usuarios ADD COLUMN enfoque_genero VARCHAR(10);
ALTER TABLE usuarios ADD COLUMN mujer_cabeza_familia BOOLEAN DEFAULT FALSE;
ALTER TABLE usuarios ADD COLUMN discapacidad BOOLEAN DEFAULT FALSE;
ALTER TABLE usuarios ADD COLUMN victima_conflicto_armado BOOLEAN DEFAULT FALSE;
ALTER TABLE usuarios ADD COLUMN poblacion_migrante BOOLEAN DEFAULT FALSE;
ALTER TABLE usuarios ADD COLUMN pertenencia_etnica VARCHAR(50);
ALTER TABLE usuarios ADD COLUMN zona_residencia VARCHAR(10);
ALTER TABLE usuarios ADD COLUMN direccion VARCHAR(255);
ALTER TABLE usuarios ADD COLUMN telefono VARCHAR(20);
