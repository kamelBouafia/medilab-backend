-- liquibase formatted sql

-- changeset medilab:03-add-lab-test-fields
ALTER TABLE lab_tests
ADD COLUMN min_val DOUBLE PRECISION,
ADD COLUMN max_val DOUBLE PRECISION;
