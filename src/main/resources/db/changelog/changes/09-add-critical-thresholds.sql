--liquibase formatted sql

--changeset system:09-add-critical-thresholds
--comment: Add critical threshold fields to lab_tests table for CRITICAL_HIGH and CRITICAL_LOW detection

ALTER TABLE lab_tests ADD COLUMN critical_min_val DOUBLE PRECISION;
ALTER TABLE lab_tests ADD COLUMN critical_max_val DOUBLE PRECISION;

COMMENT ON COLUMN lab_tests.critical_min_val IS 'Critical low threshold - values below this are CRITICAL_LOW';
COMMENT ON COLUMN lab_tests.critical_max_val IS 'Critical high threshold - values above this are CRITICAL_HIGH';
