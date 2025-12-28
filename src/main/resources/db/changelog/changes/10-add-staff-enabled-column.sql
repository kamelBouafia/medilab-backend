-- liquibase formatted sql
-- changeset ak_bo:10-add-staff-enabled-column
ALTER TABLE staff_users ADD COLUMN enabled BOOLEAN DEFAULT TRUE;
UPDATE staff_users SET enabled = TRUE;
