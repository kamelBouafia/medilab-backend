-- liquibase formatted sql

-- changeset medilab:04-rename-pdf-url-to-path
ALTER TABLE requisitions RENAME COLUMN pdf_report_url TO pdf_object_path;
