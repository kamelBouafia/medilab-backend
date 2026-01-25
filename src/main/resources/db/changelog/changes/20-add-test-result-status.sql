-- liquibase formatted sql

-- changeset medilab:20
ALTER TABLE test_results ADD COLUMN status VARCHAR(50) DEFAULT 'RESULT_ENTERED';
UPDATE test_results SET status = 'FINALIZED' WHERE result_value IS NOT NULL AND status IS NULL;
