-- liquidbase formatted sql

-- changeset medilab:3-create-global-test-catalog
CREATE TABLE global_test_catalog (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(255) NOT NULL UNIQUE,
    category VARCHAR(255),
    default_unit VARCHAR(255),
    description TEXT
);

CREATE TABLE global_test_names (
    test_id BIGINT NOT NULL,
    name VARCHAR(255),
    language_code VARCHAR(10) NOT NULL,
    PRIMARY KEY (test_id, language_code),
    CONSTRAINT fk_global_test_catalog FOREIGN KEY (test_id) REFERENCES global_test_catalog(id) ON DELETE CASCADE
);

-- changeset medilab:4-update-lab-tests
ALTER TABLE lab_tests ADD COLUMN code VARCHAR(255);
ALTER TABLE lab_tests ADD COLUMN unit VARCHAR(255);
ALTER TABLE lab_tests ADD COLUMN description TEXT;
ALTER TABLE lab_tests ADD COLUMN global_test_id BIGINT;

ALTER TABLE lab_tests ADD CONSTRAINT fk_lab_test_global_test FOREIGN KEY (global_test_id) REFERENCES global_test_catalog(id);

-- changeset medilab:5-create-test-reference-ranges
CREATE TABLE test_reference_ranges (
    id BIGSERIAL PRIMARY KEY,
    lab_test_id BIGINT NOT NULL,
    min_age INTEGER,
    max_age INTEGER,
    gender VARCHAR(10),
    min_val DOUBLE PRECISION,
    max_val DOUBLE PRECISION,
    critical_min DOUBLE PRECISION,
    critical_max DOUBLE PRECISION,
    abnormal_min DOUBLE PRECISION,
    abnormal_max DOUBLE PRECISION,
    CONSTRAINT fk_test_reference_ranges_lab_test FOREIGN KEY (lab_test_id) REFERENCES lab_tests(id) ON DELETE CASCADE
);
