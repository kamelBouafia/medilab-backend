-- liquibase formatted sql

-- changeset medilab:03-add-pdf-fields-to-requisitions
ALTER TABLE requisitions ADD COLUMN IF NOT EXISTS pdf_report_url VARCHAR(500);
ALTER TABLE requisitions ADD COLUMN IF NOT EXISTS pdf_generated_at TIMESTAMP;
