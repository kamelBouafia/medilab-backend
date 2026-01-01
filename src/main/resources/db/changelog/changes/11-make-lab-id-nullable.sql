-- liquibase formatted sql

-- changeset ak_bo:11
ALTER TABLE staff_users ALTER COLUMN lab_id DROP NOT NULL;
