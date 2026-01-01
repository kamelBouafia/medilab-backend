-- liquibase formatted sql
-- changeset ak_bo:13-global-test-catalog-schema-update endDelimiter:/

-- Add reference range columns to global_test_catalog if they don't exist
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'global_test_catalog' AND column_name = 'default_min_val') THEN
        ALTER TABLE global_test_catalog ADD COLUMN default_min_val DOUBLE PRECISION;
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'global_test_catalog' AND column_name = 'default_max_val') THEN
        ALTER TABLE global_test_catalog ADD COLUMN default_max_val DOUBLE PRECISION;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'global_test_catalog' AND column_name = 'default_critical_min') THEN
        ALTER TABLE global_test_catalog ADD COLUMN default_critical_min DOUBLE PRECISION;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'global_test_catalog' AND column_name = 'default_critical_max') THEN
        ALTER TABLE global_test_catalog ADD COLUMN default_critical_max DOUBLE PRECISION;
    END IF;
    
    -- Ensure default_unit exists (it was in the CREATE, checking just in case)
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'global_test_catalog' AND column_name = 'default_unit') THEN
        ALTER TABLE global_test_catalog ADD COLUMN default_unit VARCHAR(50);
    END IF;
END $$;
/

-- Add foreign key to lab_tests if not exists
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'lab_tests' AND column_name = 'global_test_id'
    ) THEN
        ALTER TABLE lab_tests ADD COLUMN global_test_id BIGINT REFERENCES global_test_catalog(id);
    END IF;
END $$;
/
