-- 新データベースのスキーマ定義

DROP TABLE IF EXISTS new_customers CASCADE;

CREATE TABLE new_customers (
    id BIGSERIAL PRIMARY KEY,
    customer_id VARCHAR(20) NOT NULL UNIQUE,
    full_name VARCHAR(100) NOT NULL,
    email_address VARCHAR(100),
    phone_number VARCHAR(20),
    full_address VARCHAR(255),
    zip_code VARCHAR(10),
    registration_date TIMESTAMP,
    is_active BOOLEAN,
    migrated_at TIMESTAMP,
    source_id BIGINT,
    gender VARCHAR(10)
);

DROP TABLE IF EXISTS new_companies CASCADE;

CREATE TABLE new_companies (
    id BIGSERIAL PRIMARY KEY,
    company_id VARCHAR(20) NOT NULL UNIQUE,
    company_name VARCHAR(200) NOT NULL,
    representative VARCHAR(100),
    industry_category VARCHAR(50),
    employees INTEGER,
    capital_amount BIGINT,
    foundation_date DATE,
    office_address VARCHAR(300),
    zip_code VARCHAR(10),
    contact_phone VARCHAR(20),
    contact_email VARCHAR(100),
    is_active BOOLEAN,
    migrated_at TIMESTAMP
);
