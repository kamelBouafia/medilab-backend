-- liquibase formatted sql

-- changeset your_name:5
ALTER TABLE audit_log ALTER COLUMN user_id DROP NOT NULL;
