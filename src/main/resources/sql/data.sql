-- src/main/resources/sql/data.sql

-- Sample data for the todo table
-- Note: If spring.jpa.hibernate.ddl-auto is 'create' or 'create-drop',
-- this file will be run automatically.
-- For 'update' or 'validate', you might need spring.sql.init.mode=always in application.properties.

INSERT INTO todo (title, deadline, status) VALUES
('Spring Bootプロジェクトのセットアップ', '2024-08-15', 'COMPLETED'),
('ドメインモデルの設計と実装', '2024-08-16', 'COMPLETED'),
('アプリケーションサービス層の追加', '2024-08-17', 'IN_PROGRESS'),
('MySQLへの移行作業', CURDATE(), 'PENDING'),
('Thymeleafテンプレートの修正（ステータス表示）', CURDATE() + INTERVAL 1 DAY, 'PENDING'),
('単体テストの拡充', '2024-08-20', 'PENDING'),
('結合テストの作成', '2024-08-22', 'PENDING'),
('ドキュメント作成', '2024-08-25', 'CANCELLED');

INSERT INTO todo (title, status) VALUES
('牛乳を買う', 'PENDING'),
('部屋の掃除をする', 'IN_PROGRESS');
