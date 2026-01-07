SET @col_exists := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'video'
      AND COLUMN_NAME = 'seccion'
);

SET @stmt := IF(
    @col_exists = 0,
    'ALTER TABLE video ADD COLUMN seccion VARCHAR(100) NULL',
    'SELECT 1'
);

PREPARE s FROM @stmt;
EXECUTE s;
DEALLOCATE PREPARE s;
