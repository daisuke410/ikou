-- 旧データベースのスキーマ定義

DROP TABLE IF EXISTS old_customers CASCADE;

CREATE TABLE old_customers (
    id BIGSERIAL PRIMARY KEY,
    customer_code VARCHAR(20) NOT NULL,
    customer_name VARCHAR(100) NOT NULL,
    email VARCHAR(100),
    phone VARCHAR(20),
    address VARCHAR(255),
    postal_code VARCHAR(10),
    created_at TIMESTAMP,
    status VARCHAR(20),
    gender_code INTEGER
);
