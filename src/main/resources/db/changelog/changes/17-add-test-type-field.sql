-- Add type column to lab_tests table
ALTER TABLE lab_tests ADD COLUMN "type" VARCHAR(20) DEFAULT 'IN_HOUSE';
COMMENT ON COLUMN lab_tests."type" IS 'Type of the test: IN_HOUSE or OUTSOURCED';

-- Update existing tests to IN_HOUSE
UPDATE lab_tests SET "type" = 'IN_HOUSE' WHERE "type" IS NULL;

-- Make it non-nullable if desired, but default is fine for now.
ALTER TABLE lab_tests ALTER COLUMN "type" SET NOT NULL;
