-- liquibase formatted sql
-- changeset ak_bo:12-add-gdpr-consent-fields
ALTER TABLE staff_users ADD COLUMN gdpr_accepted BOOLEAN DEFAULT FALSE;
ALTER TABLE staff_users ADD COLUMN gdpr_accepted_at TIMESTAMP;

ALTER TABLE patients ADD COLUMN gdpr_accepted BOOLEAN DEFAULT FALSE;
ALTER TABLE patients ADD COLUMN gdpr_accepted_at TIMESTAMP;
