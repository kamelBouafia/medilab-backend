-- liquibase formatted sql

-- changeset medilab:08-add-lab-language
ALTER TABLE labs ADD COLUMN default_language VARCHAR(5) DEFAULT 'en' NOT NULL;
