SET @idx_ucv := (
    SELECT t.index_name
    FROM (
        SELECT index_name, GROUP_CONCAT(column_name ORDER BY seq_in_index) AS cols
        FROM information_schema.statistics
        WHERE table_schema = DATABASE()
          AND table_name = 'video_progreso'
          AND non_unique = 0
        GROUP BY index_name
    ) t
    WHERE t.cols = 'usuario_id,curso_id,video_id'
    LIMIT 1
);

SET @sql_add := IF(
    @idx_ucv IS NULL,
    'ALTER TABLE video_progreso ADD CONSTRAINT uk_video_progreso_usuario_curso_video UNIQUE (usuario_id, curso_id, video_id)',
    'SELECT 1'
);

PREPARE stmt_add FROM @sql_add;
EXECUTE stmt_add;
DEALLOCATE PREPARE stmt_add;

SET @idx_uc := (
    SELECT t.index_name
    FROM (
        SELECT index_name, GROUP_CONCAT(column_name ORDER BY seq_in_index) AS cols
        FROM information_schema.statistics
        WHERE table_schema = DATABASE()
          AND table_name = 'video_progreso'
          AND non_unique = 0
        GROUP BY index_name
    ) t
    WHERE t.cols = 'usuario_id,curso_id'
    LIMIT 1
);

SET @sql_drop := IF(
    @idx_uc IS NULL,
    'SELECT 1',
    CONCAT('ALTER TABLE video_progreso DROP INDEX ', @idx_uc)
);

PREPARE stmt_drop FROM @sql_drop;
EXECUTE stmt_drop;
DEALLOCATE PREPARE stmt_drop;
