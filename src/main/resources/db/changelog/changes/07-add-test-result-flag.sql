-- liquidbase formatted sql

-- changeset medilab:7-add-test-result-flag
ALTER TABLE test_results ADD COLUMN flag VARCHAR(20);
