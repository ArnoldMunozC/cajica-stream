ALTER TABLE video_progreso
    ADD COLUMN completado TINYINT(1) NOT NULL DEFAULT 0,
    ADD COLUMN fecha_completado TIMESTAMP NULL;
