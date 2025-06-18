-- src/main/resources/sql/schema.sql

-- Drop table if it exists to ensure a clean state, useful for development
-- For production, use more sophisticated migration tools like Flyway or Liquibase
DROP TABLE IF EXISTS todo;

CREATE TABLE todo (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'TODOアイテムのID',
    title VARCHAR(255) NOT NULL COMMENT 'TODOアイテムのタイトル',
    deadline DATE COMMENT 'TODOアイテムの期限日',
    status VARCHAR(50) NOT NULL COMMENT 'TODOアイテムのステータス (例: PENDING, IN_PROGRESS, COMPLETED, CANCELLED)'
    -- created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '作成日時', -- 任意: 監査情報
    -- updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最終更新日時' -- 任意: 監査情報
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='TODOアイテム情報を格納するテーブル';

-- Indexes (optional, but good for performance on larger tables)
-- CREATE INDEX idx_status ON todo (status);
-- CREATE INDEX idx_deadline ON todo (deadline);
