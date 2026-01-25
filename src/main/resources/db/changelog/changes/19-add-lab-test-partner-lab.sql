-- Add partner_lab_id column to lab_tests table
ALTER TABLE lab_tests ADD COLUMN partner_lab_id BIGINT;
ALTER TABLE lab_tests ADD CONSTRAINT fk_lab_test_partner_lab FOREIGN KEY (partner_lab_id) REFERENCES labs(id);
COMMENT ON COLUMN lab_tests.partner_lab_id IS 'Reference to the partner lab for outsourced tests';
